package cgl.iotrobots.slam.core.scanmatcher;

import cgl.iotrobots.slam.core.grid.GMap;
import cgl.iotrobots.slam.core.sensor.RangeReading;
import cgl.iotrobots.slam.core.sensor.RangeSensor;
import cgl.iotrobots.slam.core.sensor.Sensor;
import cgl.iotrobots.slam.core.utils.OrientedPoint;
import cgl.iotrobots.slam.core.utils.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ScanMatcherProcessor {
    private static Logger LOG = LoggerFactory.getLogger(ScanMatcherProcessor.class);

    ScanMatcher m_matcher;
    boolean m_computeCovariance;
    boolean m_first;
    Map<String, Sensor> m_sensorMap;
    double m_regScore, m_critScore;
    int m_beams;
    double m_maxMove;
    //state
    GMap m_map;
    OrientedPoint<Double> m_pose;
    OrientedPoint<Double> m_odoPose;
    int  m_count;

    boolean useICP;

    public ScanMatcherProcessor(GMap m) {
        m_map = new GMap(m.getCenter(), m.getWorldSizeX(), m.getWorldSizeY(), m.getResolution());
        m_pose = new OrientedPoint<Double>(0.0,0.0,0.0);
        m_regScore=300;
        m_critScore=.5*m_regScore;
        m_maxMove=1;
        m_beams=0;
        m_computeCovariance=false;
        //m_eigenspace=gsl_eigen_symmv_alloc(3);
        useICP=false;
    }

    public ScanMatcherProcessor
            (double xmin, double ymin, double xmax, double ymax, double delta, double patchdelta) {
        m_map = new GMap(new Point<Double>((xmax+xmin)*.5, (ymax+ymin)*.5), xmax-xmin, ymax-ymin, patchdelta);
        m_pose = new OrientedPoint<Double>(0.0,0.0,0.0);
        m_regScore=300;
        m_critScore=.5*m_regScore;
        m_maxMove=1;
        m_beams=0;
        m_computeCovariance=false;
        //m_eigenspace=gsl_eigen_symmv_alloc(3);
        useICP=false;
    }

    void setSensorMap(Map<String, Sensor> smap, String sensorName){
        m_sensorMap = smap;

        RangeSensor rangeSensor= (RangeSensor) m_sensorMap.get(sensorName);
        assert(rangeSensor != null && rangeSensor.beams().size() > 0);

        m_beams=rangeSensor.beams().size();
        double[] angles=new double[rangeSensor.beams().size()];
        for (int i=0; i<m_beams; i++){
            angles[i]=rangeSensor.beams().get(i).pose.theta;
        }
        m_matcher.setLaserParameters(m_beams, angles, rangeSensor.getPose());
    }

    public void init(){
        m_first=true;
        m_pose= new OrientedPoint<Double>(0.0,0.0,0.0);
        m_count=0;
    }

    void processScan(RangeReading reading){
        /**retireve the position from the reading, and compute the odometry*/
        OrientedPoint<Double> relPose = reading.getPose();
        if (m_count == 0) {
            m_odoPose = relPose;
        }

        //compute the move in the scan m_matcher
        //reference frame

        OrientedPoint<Double> move = OrientedPoint.minus(relPose,m_odoPose);
        double dth=m_odoPose.theta-m_pose.theta;
        // cout << "rel-move x="<< move.x <<  " y=" << move.y << " theta=" << move.theta << endl;

        double lin_move= OrientedPoint.mulD(move, move);
        if (lin_move>m_maxMove){
            LOG. "Too big jump in the log file: " << lin_move << endl;
            cerr << "relPose=" << relPose.x << " " <<relPose.y << endl;
            cerr << "ignoring" << endl;
            return;
            //assert(0);
            dth=0;
            move.x=move.y=move.theta=0;
        }

        double s=sin(dth), c=cos(dth);
        OrientedPoint dPose;
        dPose.x=c*move.x-s*move.y;
        dPose.y=s*move.x+c*move.y;
        dPose.theta=move.theta;

        #ifdef SCANMATHCERPROCESSOR_DEBUG
        cout << "abs-move x="<< dPose.x <<  " y=" << dPose.y << " theta=" << dPose.theta << endl;
        #endif
                m_pose=m_pose+dPose;
        m_pose.theta=atan2(sin(m_pose.theta), cos(m_pose.theta));

        #ifdef SCANMATHCERPROCESSOR_DEBUG
        cout << "StartPose: x="
                << m_pose.x << " y=" << m_pose.y << " theta=" << m_pose.theta << endl;
        #endif

                m_odoPose=relPose; //update the past pose for the next iteration


        //FIXME here I assume that everithing is referred to the center of the robot,
        //while the offset of the laser has to be taken into account

        assert(reading.size()==m_beams);
/*
	double * plainReading = new double[m_beams];
#ifdef SCANMATHCERPROCESSOR_DEBUG
	cout << "PackedReadings ";
#endif
	for(unsigned int i=0; i<m_beams; i++){
		plainReading[i]=reading[i];
#ifdef SCANMATHCERPROCESSOR_DEBUG
		cout << plainReading[i] << " ";
#endif
	}
*/
        double * plainReading = new double[m_beams];
        reading.rawView(plainReading, m_map.getDelta());


        #ifdef SCANMATHCERPROCESSOR_DEBUG
        cout << endl;
        #endif
        //the final stuff: scan match the pose
        double score=0;
        OrientedPoint newPose=m_pose;
        if (m_count){
            if(m_computeCovariance){
                ScanMatcher::CovarianceMatrix cov;
                score=m_matcher.optimize(newPose, cov, m_map, m_pose, plainReading);
                        /*
			gsl_matrix* m=gsl_matrix_alloc(3,3);
			gsl_matrix_set(m,0,0,cov.xx); gsl_matrix_set(m,0,1,cov.xy); gsl_matrix_set(m,0,2,cov.xt);
			gsl_matrix_set(m,1,0,cov.xy); gsl_matrix_set(m,1,1,cov.yy); gsl_matrix_set(m,1,2,cov.yt);
			gsl_matrix_set(m,2,0,cov.xt); gsl_matrix_set(m,2,1,cov.yt); gsl_matrix_set(m,2,2,cov.tt);
			gsl_matrix* evec=gsl_matrix_alloc(3,3);
			gsl_vector* eval=gsl_vector_alloc(3);
                        */
                double m[3][3];
                double evec[3][3];
                double eval[3];
                m[0][0] = cov.xx;
                m[0][1] = cov.xy;
                m[0][2] = cov.xt;
                m[1][0] = cov.xy;
                m[1][1] = cov.yy;
                m[1][2] = cov.yt;
                m[2][0] = cov.xt;
                m[2][1] = cov.yt;
                m[2][2] = cov.tt;

                //gsl_eigen_symmv (m, eval,  evec, m_eigenspace);
                eigen_decomposition(m,evec,eval);
                #ifdef SCANMATHCERPROCESSOR_DEBUG
                //cout << "evals=" << gsl_vector_get(eval, 0) <<  " " << gsl_vector_get(eval, 1)<< " " << gsl_vector_get(eval, 2)<<endl;
                cout << "evals=" << eval[0] <<  " " << eval[1]<< " " << eval[2]<<endl;
                #endif
                //gsl_matrix_free(m);
                //gsl_matrix_free(evec);
                //gsl_vector_free(eval);
            } else {
                if (useICP){
                    cerr << "USING ICP" << endl;
                    score=m_matcher.icpOptimize(newPose, m_map, m_pose, plainReading);
                }else
                    score=m_matcher.optimize(newPose, m_map, m_pose, plainReading);
            }


        }
        //...and register the scan
        if (!m_count || score<m_regScore){
            #ifdef SCANMATHCERPROCESSOR_DEBUG
            cout << "Registering" << endl;
            #endif
            m_matcher.invalidateActiveArea();
            if (score<m_critScore){
                #ifdef SCANMATHCERPROCESSOR_DEBUG
                cout << "New Scan added, using odo pose" << endl;
                #endif
                m_matcher.registerScan(m_map, m_pose, plainReading);
            } else {
                m_matcher.registerScan(m_map, newPose, plainReading);
                #ifdef SCANMATHCERPROCESSOR_DEBUG
                cout << "New Scan added, using matched pose" << endl;
                #endif
            }
        }

        #ifdef SCANMATHCERPROCESSOR_DEBUG
        cout << " FinalPose: x="
                << newPose.x << " y=" << newPose.y << " theta=" << newPose.theta << endl;
        cout << "score=" << score << endl;
        #endif
                m_pose=newPose;
        delete [] plainReading;
        m_count++;
    }
}
