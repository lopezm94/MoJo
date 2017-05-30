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
          for (int j=0; j<labels.size(); j++) {
            Data elem = Data.parse(record.get(j));
            result.put(i-1,labels.get(j),elem);
          }
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex.getMessage());
      }
      return result;
    }
  }
  
  public static class createTable extends SpecialFunc {
    private static final int nparams = 1;
    private static final String funcname = "create_table";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams, args);
      assert args.get(0).getType().equals("List");
      ListData list = (ListData) args.get(0);
      TableData result = new TableData(list);
      return result;
    }
  }

  public static class columNames extends SpecialFunc {
    private static final int nparams = 1;
    private static final String funcname = "column_names";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams, args);
      assert args.get(0).getType().equals("Table");
      TableData table = (TableData) args.get(0);
      return table.getColumnNames();
    }
  }
  
  
}
