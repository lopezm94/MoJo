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
      "read_file", "write_file", "create_table", "column_names",
      "add_row", "add_row!", "sample", "add_column", "add_column!",
      "sort", "merge"
      }));
    functions = new HashMap<String, SpecialFunc>();
    functions.put("read_file", new SpecialFunc.ReadFile());
    functions.put("write_file", new SpecialFunc.WriteFile());
    functions.put("create_table", new SpecialFunc.CreateTable());
    functions.put("column_names", new SpecialFunc.ColumnNames());
    functions.put("add_row!", new SpecialFunc.AddRow());
    functions.put("add_row", new SpecialFunc.AddRowCopy());
    functions.put("sample", new SpecialFunc.Sample());
    functions.put("add_column!", new SpecialFunc.AddColumn());
    functions.put("add_column", new SpecialFunc.AddColumnCopy());
    functions.put("sort", new SpecialFunc.Sort());
    functions.put("merge", new SpecialFunc.Merge());
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
