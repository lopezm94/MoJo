import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class Functions{

  public class ReadFile extends SpecialFunc {
        private int nparams = 1;
        private String fname = "read_file";
        public ReadFile(){
            //Nothing
        }
        public Data call(ArrayList<Data> args) {
            checkParams(fname, nparams, args);
            return new VoidData();
        }
  }

}
