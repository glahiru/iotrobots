package cgl.iotrobots.st;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageReceiver {
    private static Logger LOG = LoggerFactory.getLogger(MessageReceiver.class);

    private Channel channel;

    private Connection conn;

    private BlockingQueue inQueue;

    private String queueName;

    private boolean autoAck = false;

    private Address []addresses;

    private String url;

    private ExecutorService executorService;

    private String exchangeName;

    private String routingKey;

    public MessageReceiver(BlockingQueue inQueue,
                            String queueName,
                            ExecutorService executorService,
                            Address []addresses,
                            String url) {
        this.inQueue = inQueue;
        this.executorService = executorService;
        this.queueName = queueName;
        this.addresses = addresses;
        this.url = url;
    }

    public void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            if (addresses == null) {
                factory.setUri(url);
                if (executorService != null) {
                    conn = factory.newConnection(executorService);
                } else {
                    conn = factory.newConnection();
                }
            } else {
                if (executorService != null) {
                    conn = factory.newConnection(executorService, addresses);
                } else {
                    conn = factory.newConnection(addresses);
                }
            }

            channel = conn.createChannel();

            if (exchangeName != null && routingKey != null) {
                channel.exchangeDeclare(exchangeName, "direct", false);
                channel.queueDeclare(this.queueName, true, false, false, null);
                channel.queueBind(queueName, exchangeName, routingKey);
            }

            channel.basicConsume(queueName, autoAck, "myConsumerTag",
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body)
                                throws IOException {
                            long deliveryTag = envelope.getDeliveryTag();
                            System.out.println(body);
                            inQueue.offer(body);
                            channel.basicAck(deliveryTag, false);
                        }
                    });
        } catch (IOException e) {
            String msg = "Error consuming the message";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (Exception e) {
            String msg = "Error connecting to broker";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public void stop() {
        try {
            if (channel != null) {
                channel.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (IOException e) {
            LOG.error("Error closing the rabbit MQ connection", e);
        }
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public static void main(String[] args) {
        MessageReceiver receiver = new MessageReceiver(new LinkedBlockingQueue(), "drone_frame", null, null, "amqp://localhost:5672");
        receiver.start();
    }
}
