import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpClientTest {

  private HttpClient httpClient;
  @BeforeEach
  void setUp() {

    String[] comments = {"test1", "test2", "test3"};
    this.httpClient = new HttpClient("test", 1, new CountDownLatch(1), comments, new Counter());
    this.httpClient.setComments(comments);
  }

  @Test
  void buildJson() {
    String json = this.httpClient.buildJson();
    System.out.println(json);
  }
}