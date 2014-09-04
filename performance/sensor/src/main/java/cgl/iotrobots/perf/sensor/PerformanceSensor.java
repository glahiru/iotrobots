package cgl.iotrobots.perf.sensor;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.msg.MessageContext;
import cgl.iotcloud.core.sensorsite.SiteContext;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.TransportConstants;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class PerformanceSensor extends AbstractSensor {
    private static Logger LOG = LoggerFactory.getLogger(PerformanceSensor.class);

    public static final String PERF_SEND_DATA_ROUTING_KEY = "perf_send_data";
    public static final String PERF_SEND_DATA_QUEUE_NAME = "perf_send_data";
    public static final String PERF_RECV_QUEUE_NAME = "perf_recv_data";
    public static final String PERF_RECV_ROUTING_KEY = "perf_recv_data";
    public static final String DATA_SENDER = "data_sender";
    public static final String DATA_RECEIVER = "data_receiver";
    private static final String PERF_EXCHANGE = "perf";

    public static final String TRP_ARG = "trp";

    private boolean run = true;

    LatencyWriter latencyWriter;

    public static void main(String[] args) {
        Map<String, String> properties = getProperties(args);
        SensorSubmitter.submitSensor(properties, new java.io.File(PerformanceSensor.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath()).getName(),
                PerformanceSensor.class.getCanonicalName(), Arrays.asList("local"));
    }

    @Override
    public Configurator getConfigurator(Map map) {
        return new STSensorConfigurator();
    }

    @Override
    public void open(SensorContext context) {
        final Channel sendChannel = context.getChannel(TransportConstants.TRANSPORT_RABBITMQ, DATA_SENDER);
        final Channel receiveChannel = context.getChannel(TransportConstants.TRANSPORT_RABBITMQ, DATA_RECEIVER);

        latencyWriter = new LatencyWriter("/tmp/latency.txt");

        Map conf = Utils.readStreamConfig();
        final TestSender sender = new TestSender(sendChannel, conf);
        Thread t = new Thread(sender);
        t.start();

        startListen(receiveChannel, new cgl.iotcloud.core.MessageReceiver() {
            @Override
            public void onMessage(Object message) {
                if (message instanceof MessageContext) {
                    String time = (String) ((MessageContext) message).getProperties().get("time");
                    long lat = System.currentTimeMillis() - Long.parseLong(time);

                    Test t = sender.getCurrentTest();
                    if (t !=  null) {
                        t.addResult(lat);
                    }

                    LOG.info("Message received " + message.toString());

                } else {
                    LOG.error("Unexpected message");
                }
            }
        });
        LOG.info("Received request for opening sensor: {} with id: {}", context.getSensorID());
    }

    public class TestSender implements Runnable {
        private Channel sendChannel;

        private List<Integer> messageSizes;

        private List<Integer> messageRates;

        String zkServers;

        int noMessages;

        int noSensors;

        Test currentTest;

        public TestSender(Channel sendChannel, Map conf) {
            this.sendChannel = sendChannel;

            Object o = conf.get(Constants.MSG_RATES);
            if (o instanceof List) {
                messageRates = (List<Integer>) o;
            }

            o = conf.get(Constants.MSG_SIZES);
            if (o instanceof List) {
                messageSizes = (List<Integer>) o;
            }
            zkServers = conf.get(Constants.ZK_SERVERS).toString();
            noMessages = (int) conf.get(Constants.NO_MSGS);
            noSensors = (int) conf.get(Constants.NO_SENSORS);
        }

        @Override
        public void run() {
            while (run) {
                for (int msgRate : messageRates) {
                    for (int msgSize : messageSizes) {
                        // create a test
                        currentTest = new Test(noMessages, msgSize, msgRate, zkServers, noSensors, latencyWriter);
                        currentTest.start();

                        BlockingQueue<byte []> messages = currentTest.getMessages();
                        for (int i = 0; i < noMessages; i++) {
                            if (currentTest.canContinue()) {
                                try {
                                    byte[] body = messages.take();
                                    Map<String, Object> props = new HashMap<String, Object>();
                                    props.put("time", Long.toString(System.currentTimeMillis()));
                                    sendChannel.publish(body, props);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                break;
                            }
                        }
                        currentTest.stop();
                    }
                }
            }
        }

        public Test getCurrentTest() {
            return currentTest;
        }
    }

    @Override
    public void close() {
        run = false;
        super.close();
    }

    @SuppressWarnings("unchecked")
    private class STSensorConfigurator extends AbstractConfigurator {
        @Override
        public SensorContext configure(SiteContext siteContext, Map conf) {
            SensorContext context = new SensorContext("perf_sensor");

            Map<String, String> sendProps = new HashMap<String, String>();
            sendProps.put("exchange", PERF_EXCHANGE);
            sendProps.put("routingKey", PERF_SEND_DATA_ROUTING_KEY);
            sendProps.put("queueName", PERF_SEND_DATA_QUEUE_NAME);
            Channel sendChannel = createChannel(DATA_SENDER, sendProps, Direction.OUT, 1024);

            Map<String, String> receiveProps = new HashMap<String, String>();
            receiveProps.put("queueName", PERF_RECV_QUEUE_NAME);
            receiveProps.put("exchange", PERF_EXCHANGE);
            receiveProps.put("routingKey", PERF_RECV_ROUTING_KEY);
            Channel receiveChannel = createChannel(DATA_RECEIVER, receiveProps, Direction.IN, 1024);

            context.addChannel(TransportConstants.TRANSPORT_RABBITMQ, sendChannel);
            context.addChannel(TransportConstants.TRANSPORT_RABBITMQ, receiveChannel);

            return context;
        }
    }

    private static Map<String, String> getProperties(String []args) {
        Map<String, String> conf = new HashMap<String, String>();
        Options options = new Options();
        options.addOption(TRP_ARG, true, "k or r");
        CommandLineParser commandLineParser = new BasicParser();
        try {
            CommandLine cmd = commandLineParser.parse(options, args);
            String trp = cmd.getOptionValue(TRP_ARG);
            conf.put(TRP_ARG, trp);

            return conf;
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("sensor", options );
        }
        return null;
    }
}
