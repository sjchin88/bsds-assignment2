import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class LikeRecorder {
  private static final String EXCHANGE_NAME = "swipes";
  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "direct");
    // Create a non-durable, exclusive, autodelete queue for the exchange
    String queueName = channel.queueDeclare().getQueue();
    // Create a binding between the queue and the exchange, using the binding key "left" and "right"
    channel.queueBind(queueName, EXCHANGE_NAME, "left");
    channel.queueBind(queueName, EXCHANGE_NAME, "right");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + message + "'");
    };
    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
  }
}
