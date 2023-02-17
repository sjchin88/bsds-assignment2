import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Represent a ClientDriver class for generating HttpClient threads and number of requests
 * without recording the results
 */
public class ClientDriver {
  // protected final static int[] NUMTHREADS_LIST = new int[] { 1, 1, 1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 500, 1000};
  // protected final static int[] REQUEST_LIST = new int[] {1, 100, 10_000, 100_000, 200_000, 300_000, 400_000, 500_000, 500_000, 500_000, 500_000, 500_000, 500_000, 500_000, 500_000, 500_000};
  protected final static int[] NUMTHREADS_LIST = new int[] { 1,  1, 10, 20, 50,  100, 200, 500, 1000};
  protected final static int[] REQUEST_LIST = new int[] {10,  10_000, 100_000, 200_000,  500_000,  500_000, 500_000, 500_000, 500_000};
  // protected final static String[] COMMENTS = {"Hi", "How are you", "I Like You"};
  protected final static String URL = "http://localhost:8091/Server_war_exploded/swipe";


  /**
   * Main method to initiate the threads
   * @param args from CLI, not required at here
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, IOException {
    System.out.println("Run without recording");
    List<String[]> runRecords = new ArrayList<>();
    final String[] headers = new String[]{"Number of Threads", "Number of requests", "Time taken", "Throughput per second"};
    runRecords.add(headers);
    for (int idx = 0; idx < 1; idx++) {
      int numthreads = NUMTHREADS_LIST[idx];
      int totalRequests = REQUEST_LIST[idx];
      CountDownLatch countDownLatch = new CountDownLatch(numthreads);
      Counter counter = new Counter();
      int requestPerThread = totalRequests / numthreads;
      Long start = System.currentTimeMillis();
      for (int i = 0; i < numthreads; i++){
        Thread thread = new Thread(new HttpClient(URL, requestPerThread, countDownLatch, counter));
        thread.start();
      }
      countDownLatch.await();
      Long end = System.currentTimeMillis();
      Long timeTaken = end - start;
      printOutput(counter, timeTaken, idx, runRecords);
    }
    RecordProcessor recordProcessor = new RecordProcessor(null, null);
    recordProcessor.storeResult(runRecords, "RunsSummary.csv");
    // ClientRecordingDriver.main(new String[]{});
  }

  /**
   * Method to print the output after finished sending the request
   *
   * @param counter    Counter object storing count success and failure
   * @param timeTaken  time taken (wall time)
   * @param idx        idx of current run
   * @param runRecords List of String to store record of each run
   */
  public static void printOutput(Counter counter, Long timeTaken, Integer idx,
      List<String[]> runRecords){

    System.out.println("Test " + (idx + 1));
    System.out.println("Number of threads used: " + NUMTHREADS_LIST[idx]);
    System.out.println("Number of successful requests: " + counter.getCountSuccess());
    System.out.println("Number of unsuccessful requests: " + counter.getCountFailure());
    System.out.println("Total run time (wall time) taken = " + timeTaken + "ms");
    double throughput = REQUEST_LIST[idx]/(timeTaken/1000.0);
    System.out.println("Total Throughput in requests per second = " + throughput);
    String[] record = new String[] {String.valueOf(NUMTHREADS_LIST[idx]),
        String.valueOf(REQUEST_LIST[idx]), String.valueOf(timeTaken), String.valueOf(throughput)};
    runRecords.add(record);
  }
}
