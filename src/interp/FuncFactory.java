package interp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;

public class FuncFactory {

  HashSet<String> fnames;
  HashMap<String, SpecialFunc> functions;

  public static FuncFactory factory;

  public static FuncFactory getinstance() {
    if (factory == null) return new FuncFactory();
    else return factory;
  }

  private FuncFactory() {
    fnames = new HashSet<String>(Arrays.asList(new String[] {
      "read_file", "write_file"
      }));
    functions = new HashMap<String, SpecialFunc>();
    functions.put("read_file", new SpecialFunc.ReadFile());
    functions.put("write_file", new SpecialFunc.WriteFile());
  }

  public boolean contains(String fname) {
    return fnames.contains(fname);
  }

  public SpecialFunc getFunction(String fname) {
    SpecialFunc func = functions.get(fname);
    if (func == null)
      throw new RuntimeException("Function "+fname+" not defined\n");
    return func;
  }
}
