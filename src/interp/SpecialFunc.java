package interp;
import java.io.*;
import java.util.*;
import org.apache.commons.csv.*;

public abstract class SpecialFunc {

  public abstract Data call(ArrayList<Data> args);

  static void checkParams(String funcname, int n, ArrayList<Data> args) {
    if (n != args.size()) {
      throw new RuntimeException (
        "Incorrect number of parameters calling function " + funcname + "\n"
        );
    }
  }

  public static class ReadFile extends SpecialFunc {
    private static final int nparams = 1;
    private static final String funcname = "read_file";
    public Data call(ArrayList<Data> args) {
      TableData result = new TableData();
      checkParams(funcname, nparams, args);
      String filepath = Data.toStringData(args.get(0)).getValue();
      //System.out.println(filepath);
      try {
        FileReader fr = new FileReader(new File(filepath));
        CSVParser parser = CSVFormat.DEFAULT.parse(fr);
        List<CSVRecord> records = parser.getRecords();
        CSVRecord header = records.get(0);
        ListData<StringData> labels = new ListData<StringData>();
        for (String col : header) {
          StringData aux = new StringData(col);
          result.addColumn(aux);
          labels.add(aux);
        }
        for (int i=1; i<records.size(); i++) {
          CSVRecord record = records.get(i);
          //System.out.println(record);
          for (int j=0; j<labels.size(); j++) {
            //System.out.println(record.get(j));
            //Data elem = Data.parse();
            //result.put(j,col,elem);
          }
        }
        //System.out.println("Done creating table");
      } catch (Exception ex) {
        throw new RuntimeException(ex.getMessage());
      }
      //System.out.println(result);
      return result;
    }
  }

}
