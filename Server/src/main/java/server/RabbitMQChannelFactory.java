package server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * RabbitMQ channel factory based on the apache pool libraries
 */
public class RabbitMQChannelFactory extends BasePooledObjectFactory<Channel> {

  // Connection for RMQ
  private final Connection rmqConnection;
  private int channelCount;

  /**
   * Create new RabbitMQ channel factory using given RabbitMQ connection
   * @param rmqConnection a valid connection to RabbitMQ
   */
  public RabbitMQChannelFactory(Connection rmqConnection) {
    this.rmqConnection = rmqConnection;
    this.channelCount = 0;
  }

  /**
   * Create the RabbitMQ channel threadsafe way
   * @return channel created
   * @throws Exception
   */
  @Override
  synchronized public Channel create() throws IOException {
    this.channelCount++;
    Channel channel = this.rmqConnection.createChannel();
    return channel;
  }

  /**
   * Wrap the channel into a PooledObject and return
   * @param channel given RabbitMQ channel
   * @return PooledObject <Channel>
   */
  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<>(channel);
  }

  /**
   * Getter for channelCount, used for debugging
   * @return channelCount
   */
  public int getChannelCount() {
    return this.channelCount;
  }
}
