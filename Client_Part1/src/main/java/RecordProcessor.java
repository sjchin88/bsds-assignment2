import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class RecordProcessor {
  /**
   * Method to store the records from the List<String> into given fileName
   * @param records record of List<String>
   * @param fileName fileName of target record file
   */
  public void storeResult(List<String[]> records, String fileName) throws IOException {
    final CSVWriter csvWriter = this.createWriter(fileName);
    csvWriter.writeAll(records);
    csvWriter.close();
  }
  /**
   * Create a new CSV Writer from given String file
   * @param fullName name of the file in String
   * @return CSV Writer file
   */
  public CSVWriter createWriter(final String fullName) {
    try{
      final File file = new File(fullName);
      // create FileWriter object with file as parameter
      final FileWriter outputfile = new FileWriter(file);

      // create CSVWriter object filewriter object as parameter
      return new CSVWriter(outputfile);
    }catch (IOException e){
      throw new RuntimeException(e);
    }
  }
}
