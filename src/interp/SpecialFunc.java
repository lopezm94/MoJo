package interp;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public interface SpecialFunc {

    public abstract Data call(ArrayList<Data> args);
    void checkParams(String fname, int n, ArrayList<Data> args);
    
    public class ReadFile implements SpecialFunc {
        private int nparams = 1;
        private String fname = "read_file";
        
        public void checkParams(String fname, int n, ArrayList<Data> args) {
            if (n != args.size()) {
                throw new RuntimeException (
                    "Incorrect number of parameters calling function " + fname + "\n"
                    );
            }
        }
        
        public Data call(ArrayList<Data> args) {
            checkParams(fname, nparams, args);
            return new VoidData();
        }
  }

}

