package cgl.iotrobots.collavoid.utils;


public class VelocitySample {
    private Vector2 velocity;
    private double distToPrefVel;

    public Vector2 getVelocity(){
        return new Vector2(this.velocity);
    }

    public double getDistToPrefVel(){
        return this.distToPrefVel;
    }

    public void setVelocity(Vector2 v){
        this.velocity=new Vector2(v);
    }

    public void setDistToPrefVel(double d){
        this.distToPrefVel=d;
    }
}
