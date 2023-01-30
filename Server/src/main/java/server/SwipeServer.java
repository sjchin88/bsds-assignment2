package server;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Server class to handle traffic to /swipe path
 */
@WebServlet(name = "SwipeServlet", value = "/swipe")
public class SwipeServer extends HttpServlet {
  private static final String URL_LEFT = "/left";
  private static final String URL_RIGHT = "/right";
  private static final int LOWER_BOUND = 0;
  private static final int SWIPER_UPPER = 5000;
  private static final int SWIPEE_UPPER = 1_000_000;

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
   * @param request
   * @param response
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
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      SwipeResponse swipeResponse = new SwipeResponse();
      swipeResponse.setMessage("invalid url");
      response.getOutputStream().print(gson.toJson(swipeResponse));
      response.getOutputStream().flush();
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
      SwipeResponse swipeResponse = new SwipeResponse();
      int swiperId  = Integer.parseInt(swipeDetail.getSwiper());
      if(swiperId < LOWER_BOUND || swiperId > SWIPER_UPPER) {
        swipeResponse.setMessage("User not found");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getOutputStream().print(gson.toJson(swipeResponse));
        response.getOutputStream().flush();
        return;
      }

      int swipeeId = Integer.parseInt(swipeDetail.getSwipee());
      if(swipeeId < LOWER_BOUND || swipeeId > SWIPEE_UPPER){
        swipeResponse.setMessage("Invalid Input");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getOutputStream().print(gson.toJson(swipeResponse));
        response.getOutputStream().flush();
        return;
      }

      if(swipeDetail.getComment() == null){
        swipeResponse.setMessage("Invalid Input");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getOutputStream().print(gson.toJson(swipeResponse));
        response.getOutputStream().flush();
        return;
      }

      response.setStatus(HttpServletResponse.SC_CREATED);
      swipeResponse.setMessage("Swipe Ok");
      response.getOutputStream().print(gson.toJson(swipeResponse));
      response.getOutputStream().flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
