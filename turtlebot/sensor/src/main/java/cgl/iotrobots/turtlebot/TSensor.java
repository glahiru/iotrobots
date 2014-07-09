package cgl.iotrobots.turtlebot;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.msg.MessageContext;
import cgl.iotcloud.core.sensorsite.SiteContext;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotrobots.turtlebot.commons.CommonsUtils;
import cgl.iotrobots.turtlebot.commons.KinectMessageReceiver;
import cgl.iotrobots.turtlebot.commons.Motion;
import org.apache.commons.cli.*;
import org.ros.node.NodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TSensor extends AbstractSensor {
    private static Logger LOG = LoggerFactory.getLogger(TSensor.class);

    public static final String BROKER_URL = "broker_url";
    public static final String LOCAL_IP = "local_ip";
    public static final String ROS_MASTER = "ros_master";

    private TurtleController controller;

    public static void main(String[] args) {
        Map<String, String> properties = getProperties(args);
        SensorSubmitter.submitSensor(properties, "iotrobots-sensor-1.0-SNAPSHOT-jar-with-dependencies.jar", TSensor.class.getCanonicalName(), Arrays.asList("local-1"));
    }

    @Override
    public Configurator getConfigurator(Map map) {
        return new STSensorConfigurator();
    }

    @Override
    public void open(SensorContext context) {
        final BlockingQueue receivingQueue = new LinkedBlockingQueue();
        final Channel sendChannel = context.getChannel("rabbitmq", "sender");
        final Channel receiveChannel = context.getChannel("rabbitmq", "receiver");

        // register with ros_java
        NodeConfiguration nodeConfiguration = null;
        String localIp = (String) context.getProperty(LOCAL_IP);
        String rosMaster = (String) context.getProperty(ROS_MASTER);

        try {
            nodeConfiguration = NodeConfiguration.newPublic("156.56.93.102", new URI("http://149.160.205.153:11311"));
        } catch (URISyntaxException e) {
            LOG.error("Failed to connect", e);
        }
        controller = new TurtleController();
        controller.start(nodeConfiguration);

        String brokerURL = (String) context.getProperty(BROKER_URL);
        KinectMessageReceiver receiver = new KinectMessageReceiver(receivingQueue, "kinect_controller", null, null, brokerURL);
        receiver.setExchangeName("kinect_frames");
        receiver.setRoutingKey("kinect_controller");
        receiver.start();

        // startSend(sendChannel, receivingQueue);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        byte[] body = (byte[]) receivingQueue.take();
                        Map<String, Object> props = new HashMap<String, Object>();
                        props.put("time", Long.toString(System.currentTimeMillis()));
                        sendChannel.publish(body, props);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();

        startListen(receiveChannel, new cgl.iotcloud.core.MessageReceiver() {
            @Override
            public void onMessage(Object message) {
                if (message instanceof MessageContext) {
                    try {
                        Motion motion = CommonsUtils.jsonToMotion(((MessageContext) message).getBody());
                        String time = (String) ((MessageContext) message).getProperties().get("time");
                        System.out.println(System.currentTimeMillis() - Long.parseLong(time));
                        controller.setMotion(motion);
                        System.out.println("Message received " + message.toString());
                    } catch (IOException e) {
                        LOG.error("Un-expected motion control message");
                    }
                } else {
                    LOG.error("Unexpected message");
                }
            }
        });
        LOG.info("Received request {}", context.getId());
    }

    @Override
    public void close() {
        super.close();
        controller.shutDown();
    }

    @SuppressWarnings("unchecked")
    private class STSensorConfigurator extends AbstractConfigurator {
        @Override
        public SensorContext configure(SiteContext siteContext, Map conf) {
            String brokerUrl = (String) conf.get(BROKER_URL);

            SensorContext context = new SensorContext(new SensorId("turtle_sensor", "general"));
            context.addProperty(BROKER_URL, brokerUrl);
            Map sendProps = new HashMap();
            sendProps.put("exchange", "turtle_sensor");
            sendProps.put("routingKey", "kinect");
            sendProps.put("queueName", "kinect");
            Channel sendChannel = createChannel("sender", sendProps, Direction.OUT, 1024);

            Map receiveProps = new HashMap();
            receiveProps.put("queueName", "control");
            receiveProps.put("exchange", "turtle_sensor");
            receiveProps.put("routingKey", "control");
            Channel receiveChannel = createChannel("receiver", receiveProps, Direction.IN, 1024);

            context.addChannel("rabbitmq", sendChannel);
            context.addChannel("rabbitmq", receiveChannel);

            return context;
        }
    }

    private static Map<String, String> getProperties(String []args) {
        Map<String, String> conf = new HashMap<String, String>();
        Options options = new Options();
        options.addOption("url", true, "URL of the AMQP Broker");
        options.addOption("ros_master", true, "Ros master URL");
        options.addOption("local_ip", true, "Local IP address");

        CommandLineParser commandLineParser = new BasicParser();
        try {
            CommandLine cmd = commandLineParser.parse(options, args);

            String url = cmd.getOptionValue("url");
            String rosMaster = cmd.getOptionValue(ROS_MASTER);
            String localIp = cmd.getOptionValue(LOCAL_IP);

            System.out.println(url);
            System.out.println(rosMaster);
            System.out.println(localIp);

            conf.put(BROKER_URL, url);
            conf.put(ROS_MASTER, rosMaster);
            conf.put(LOCAL_IP, localIp);

            return conf;
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("sensor", options );
        }
        return null;
    }
}
