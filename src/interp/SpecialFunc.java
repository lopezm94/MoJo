package interp;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public interface SpecialFunc {

  public Data call(ArrayList<Data> args);

  static void checkParams(String fname, int n, ArrayList<Data> args) {
    if (n != args.size()) {
      throw new RuntimeException (
        "Incorrect number of parameters calling function " + fname + "\n"
        );
    }
  }

  public class ReadFile implements SpecialFunc {
    private int nparams = 1;
    private String fname = "read_file";
    public Data call(ArrayList<Data> args) {
      checkParams(fname, nparams, args);
      return new VoidData();
    }
  }
}
