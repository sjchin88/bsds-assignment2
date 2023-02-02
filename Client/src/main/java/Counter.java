/**
 * Represent a counter storing the countSuccess and countFailure,
 * and control its incrementation in synchronized manner
 */
public class Counter {
  private int countSuccess;
  private int countFailure;

  /**
   * Initialize a new Counter object
   */
  public Counter() {
    this.countSuccess = 0;
    this.countFailure = 0;
  }

  /**
   * Synchronized method to add new count of success and failure from each thread to the
   * countSuccess and countFailure stored
   * @param success new count of success from the thread
   * @param failure new count of failure from the thread
   */
  public synchronized void addCount(int success, int failure) {
    this.countSuccess += success;
    this.countFailure += failure;
  }

  /**
   * Getter for the countSuccess variable
   * @return countSuccess in int
   */
  public synchronized int getCountSuccess() {
    return countSuccess;
  }

  /**
   * Getter for the countFailure variable
   * @return countFailure in int
   */
  public int getCountFailure() {
    return countFailure;
  }
}
