package server;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * Server class to handle traffic to /swipe path
 */
@WebServlet(name = "SwipeServlet", value = "/swipe")
public class SwipeServer extends HttpServlet {

  /**
   * Valid URL
   */
  private static final String URL_LEFT = "/left";
  /**
   * Valid URL
   */
  private static final String URL_RIGHT = "/right";

  /**
   * Response Message for valid swipe
   */
  private static final String SWIPE_OK = "Swipe Ok";

  /**
   * Error Message for invalid url
   */
  private static final String ERROR_URL = "invalid url";

  /**
   * Error Message for invalid inputs
   */
  private static final String ERROR_INPUT = "Invalid Input";

  /**
   * Error Message for invalid swiper
   */
  private static final String ERROR_USER = "User not found";

  /**
   * Lower bound limit for swiper and swipee id
   */
  private static final int LOWER_BOUND = 0;

  /**
   * Upper bound limit for swiper id
   */
  private static final int SWIPER_UPPER = 5000;
  /**
   * Upper bound limit for swipee id
   */
  private static final int SWIPEE_UPPER = 1_000_000;

  /**
   * number of channels to connect to RabbitMQ server
   */
  private static final int NUM_CHANNELS = 30;

  /**
   * For Apache pool example, this allows the pool size to grow to ~= the same number of concurrent threads
   * that utilize the pool. Pass to config.setMaxWait(..) method to allow this behaviour
   */
  private static final int ON_DEMAND = -1;

  /**
   * Exchange name to be used on RabbitMQ server
   */
  private static final String EXCHANGE_NAME = "swipes";

  /**
   * Address of the RabbitMQ server, change it to IP address when hosting on EC-2
   */
  private static final String SERVER = "localhost";
  private ConnectionFactory rabbitFactory;
  private RabbitMQChannelPool channelPool;

  /**
   * Set up the server class
   * @throws ServletException
   */
  @Override
  public void init() throws ServletException {
    super.init();
    // Create new connection to the rabbit MQ
    this.rabbitFactory = new ConnectionFactory();
    this.rabbitFactory.setHost(SERVER);
    Connection rabbitMQConn;
    try {
      rabbitMQConn = this.rabbitFactory.newConnection();
      System.out.println("INFO: RabbitMQ connection established");
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
    RabbitMQChannelFactory factory = new RabbitMQChannelFactory(rabbitMQConn);
    this.channelPool = new RabbitMQChannelPool(NUM_CHANNELS, factory);
  }

  /**
   * doGet method, currently return one line statement only as the assignment request to implement
   * post method only
   * @param request valid http request
   * @param response http response
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    response.getWriter().write("no get method");
  }

  /**
   * Check if the urlPath is valid
   * @param urlPath path of the url in string
   * @return boolean indicator
   */
  private boolean isUrlValid(String urlPath) {
    // validate the request url path according to the API spec
    if (!urlPath.equals(URL_LEFT) && !urlPath.equals(URL_RIGHT)){
      return false;
    }
    return true;
  }

  /**
   * doPost method, convert the request body from json object to SwipeDetail class object
   * check if input is valid, and give corresponding respond
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();
    Gson gson = new Gson();
    // check for valid url
    if (urlPath == null || urlPath.isEmpty() || !isUrlValid(urlPath)) {
      this.replyMsg(ERROR_URL, HttpServletResponse.SC_NOT_FOUND, response);
      return;
    }
    this.processRequest(request, response);
  }

  /**
   * Helper method to process the HTTP request
   * @param request
   * @param response
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
    Gson gson = new Gson();
    try {
      StringBuilder sb = new StringBuilder();
      String s;
      while((s = request.getReader().readLine()) != null) {
        sb.append(s);
      }
      SwipeDetail swipeDetail = (SwipeDetail) gson.fromJson(sb.toString(), SwipeDetail.class);

      // return if validation fail
      if(!this.validateData(swipeDetail, response)){
        return;
      }
      String swipeDirection = request.getPathInfo().substring(1);
      // format data and send the message to the channel
      Channel channel = this.channelPool.borrowObject();
      channel.exchangeDeclare(EXCHANGE_NAME,"direct");
      channel.basicPublish(EXCHANGE_NAME, swipeDirection, null, swipeDetail.toString().getBytes("UTF-8"));
      this.replyMsg(SWIPE_OK,HttpServletResponse.SC_CREATED,response);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Send HTTP response using given message and responseCode
   * @param message response message in string
   * @param responseCode HTTP response code in int
   * @param response  HttpServlet Response object
   * @throws IOException IO exception when writing into the getOutputStream
   */
  protected void replyMsg(String message, int responseCode, HttpServletResponse response)
      throws IOException {
    Gson gson = new Gson();
    SwipeResponse swipeResponse = new SwipeResponse(message);
    response.setStatus(responseCode);
    response.getOutputStream().print(gson.toJson(swipeResponse));
    response.getOutputStream().flush();
  }

  /**
   * Validate the swipe details and send HttpResponse if required
   * @param swipeDetail object containing the swipe details
   * @param response HttpServletResponse
   * @return boolean if the validation pass
   */
  protected boolean validateData(SwipeDetail swipeDetail, HttpServletResponse response)
      throws IOException {
    int swiperId  = Integer.parseInt(swipeDetail.getSwiper());
    if(swiperId < LOWER_BOUND || swiperId > SWIPER_UPPER) {
      this.replyMsg(ERROR_USER, HttpServletResponse.SC_NOT_FOUND, response);
      return false;
    }

    int swipeeId = Integer.parseInt(swipeDetail.getSwipee());
    if(swipeeId < LOWER_BOUND || swipeeId > SWIPEE_UPPER){
      this.replyMsg(ERROR_INPUT, HttpServletResponse.SC_BAD_REQUEST, response);
      return false;
    }

    if(swipeDetail.getComment() == null){
      this.replyMsg(ERROR_INPUT, HttpServletResponse.SC_BAD_REQUEST, response);
      return false;
    }
    return true;
  }
}
