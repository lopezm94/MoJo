package interp;
import java.io.*;
import java.util.*;
import org.apache.commons.csv.*;
// Imports for ANTLR
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

// Imports from Java
import org.apache.commons.cli.*; // Command Language Interface
import java.io.*;
import parser.*;


public abstract class SpecialFunc {

  public abstract Data call(ArrayList<Data> args);

  static void checkParams(String funcname, int min, int max, ArrayList<Data> args) {
    if (max < args.size() || min > args.size()) {
      throw new RuntimeException (
        "Incorrect number of parameters calling function " + funcname + "\n"
        );
    }
  }

  public static class ReadFile extends SpecialFunc {
    private static final int nparams = 1;
    private static final String funcname = "read_file";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams, nparams, args);

      TableData result = new TableData();
      String filepath = StringData.cast(args.get(0)).getValue();
      try {
        FileReader fr = new FileReader(new File(filepath));
        CSVParser parser = CSVFormat.DEFAULT.parse(fr);
        List<CSVRecord> records = parser.getRecords();
        CSVRecord header = records.get(0);
        ListData<StringData> labels = new ListData<StringData>();
        for (String col : header) {
          StringData aux = new StringData(col.trim());
          result.addColumn(aux);
          labels.add(aux);
        }
        for (int i=1; i<records.size(); i++) {
          CSVRecord record = records.get(i);
          for (int j=0; j<labels.size(); j++) {
            Data elem = Data.parse(record.get(j));
            if(Data.isType("Void", elem)) continue;
            result.put(i-1,labels.get(j),elem);
          }
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex.getMessage());
      }
      return result;
    }
  }

  public static class WriteFile extends SpecialFunc {
    private static final int nparams = 2;
    private static final String funcname = "write_file";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams, nparams, args);

      TableData table = TableData.cast(args.get(0));
      String filepath = StringData.cast(args.get(1)).getValue();
      FileWriter fileWriter = null;
      CSVPrinter csvFilePrinter = null;

      //Create the CSVFormat object with "\n" as a record delimiter
      CSVFormat csvFileFormat =
        CSVFormat.DEFAULT.withRecordSeparator("\n");

      try {

        //initialize FileWriter object
        fileWriter = new FileWriter(filepath);

        //initialize CSVPrinter object
        csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        //Print header
        ArrayList<String> labels = table.getLabels();
        csvFilePrinter.printRecord(labels);

        //Write a rows
        for (int i=0; i<table.height(); i++) {
          DictData row = table.get(i);
          List record = new ArrayList();
          for (int j=0; j<table.width(); j++) {
            Data elem = row.get(labels.get(j));
            if (Data.isType("Void", elem))
              record.add("");
            else
              record.add(elem.toString());
          }
          csvFilePrinter.printRecord(record);
        }
      } catch (Exception e) {
        System.out.println("Error in write_file");
        e.printStackTrace();
      } finally {
        try {
          fileWriter.flush();
          fileWriter.close();
          csvFilePrinter.close();
        } catch (IOException e) {
          System.out.println("Error while flushing/closing fileWriter/csvPrinter");
          e.printStackTrace();
        }
      }

      return new VoidData();
    }
  }
  

  
  public static class CreateNewTable extends SpecialFunc {
    private static final int nparams = 1;
    private static final String funcname = "create_table";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams, nparams, args);
      assert Data.isType("List",args.get(0));
      ListData list = (ListData) args.get(0);
      TableData result = new TableData(list);
      return result;
    }
  }


  //*** Importante: Cambia la definicion inicial de la funcion
  public static class Sample extends SpecialFunc {
    private static final int nparams = 2;
    private static final String funcname = "sample";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams, nparams, args);
      assert Data.isType("Integer", args.get(0));
      assert Data.isType("Table", args.get(1));
      IntegerData n = IntegerData.cast(args.get(0));
      TableData original = TableData.cast(args.get(1));
      return original.sample(n);
    }
  }

  public static class Sort extends SpecialFunc {
    private static final int nparams = 1;
    private static final String funcname = "sort";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams, nparams, args);
      assert Data.isType("Table", args.get(0))
        || Data.isType("List", args.get(0));
      Data seqCollection = args.get(0);
      if (Data.isType("Table", args.get(0)))
        return TableData.cast(seqCollection).sort();
      else
        return ListData.cast(seqCollection).sort();
    }
  }

  public static class Merge extends SpecialFunc {
    private static final int nparamsMin = 1;
    private static final int nparamsMax = 10;
    private static final String funcname = "merge";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparamsMin, nparamsMax, args);
      assert Data.isType("Table", args.get(0));
      TableData res = TableData.cast(args.get(0));
      for (int i=1; i<args.size(); i++) {
        assert Data.isType("Table", args.get(i));
        res.merge(TableData.cast(args.get(i)));
      }
      return res;
    }
  }
  
  public static class GetColumnNames extends SpecialFunc {
    private static final int nparams = 1;
    private static final String funcname = "column_names";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams, nparams, args);
      assert Data.isType("Table", args.get(0));
      TableData table = (TableData) args.get(0);
      return table.getColumnNames();
    }
  }
  
  public static class GetNumRows extends SpecialFunc{
    private static final int nparams = 1;
    private static final String funcname = "num_rows";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams , nparams , args);
      assert args.get(0).getType().equals("Table");
      TableData table = (TableData) args.get(0);
      return new IntegerData(table.height());
      
    }
  }
  
  public static class GetNumCols extends SpecialFunc{
    private static final int nparams = 1;
    private static final String funcname = "num_columns";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams , nparams , args);
      assert args.get(0).getType().equals("Table");
      TableData table = (TableData) args.get(0);
      return new IntegerData(table.width());
      
    }
  }
  
  public static class GetListLength extends SpecialFunc{
    private static final int nparams = 1;
    private static final String funcname = "length";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams , nparams , args);
      assert args.get(0).getType().equals("List");
      ListData list = (ListData) args.get(0);
      return new IntegerData(list.size());
      
    }
  }

   public static class AddNewRow extends SpecialFunc {
    private static final int nparamsMin = 1;
    private static final int nparamsMax = 10;
    private static final String funcname = "add_row!";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparamsMin, nparamsMax, args);
      assert Data.isType("Table", args.get(0));
      assert Data.isType("Dict", args.get(1));
      TableData table = (TableData) args.get(0);
      for(int i = 1; i<args.size(); ++i){
        assert args.get(i).getType().equals("Dict");
        DictData row = (DictData) args.get(i);
        table.addRow(row);
      }
      return table;
    }
  }

   public static class AddNewRowCopy extends SpecialFunc {
    private static final int nparamsMin = 1;
    private static final int nparamsMax = 10;
    private static final String funcname = "add_row";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparamsMin, nparamsMax, args);
      assert Data.isType("Table",args.get(0));
      TableData table = (TableData) args.get(0);
      TableData newTable = (TableData) table.deepClone();
      for(int i = 1; i<args.size(); ++i){
        assert Data.isType("Dict", args.get(i));
        DictData row = (DictData) args.get(i);
        newTable.addRow(row);
      }
      return newTable;
    }
  }

  public static class AddNewColumn extends SpecialFunc{
    private static final int nparams = 2;
    private static final String funcname = "add_column!";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams, nparams, args);
      assert args.get(0).getType().equals("Table");
      String type = args.get(1).getType();
      TableData table = (TableData) args.get(0);
      if(type.equals("List")){
        ListData<StringData> newCols = (ListData<StringData>) args.get(1);
        for(int i = 0; i<newCols.size(); ++i){
          table.addColumn(newCols.get(i));
        }
        return table;
      }else if(type.equals("Dict")){
        DictData newCols = (DictData) args.get(1);
        for(Map.Entry<StringData,Data> entry : newCols.entrySet()){
            table.addColumn(entry.getKey(),entry.getValue());
        }
        return table;
      }else assert false;
      return null;
    }
  }

  public static class AddNewColumnCopy extends SpecialFunc{
    private static final int nparams = 2;
    private static final String funcname = "add_column";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams , nparams , args);
      assert args.get(0).getType().equals("Table");
      String type = args.get(1).getType();
      TableData table = (TableData) args.get(0);
      TableData newTable = (TableData) table.deepClone();
      if(type.equals("List")){
        ListData<StringData> newCols = (ListData<StringData>) args.get(1);
        for(int i = 0; i<newCols.size(); ++i){
          newTable.addColumn(newCols.get(i));
        }
        return newTable;
      }else if(type.equals("Dict")){
        DictData newCols = (DictData) args.get(1);
        for(Map.Entry<StringData,Data> entry : newCols.entrySet()){
            newTable.addColumn(entry.getKey(),entry.getValue());
        }
        return newTable;
      }else assert false;
      return null;
    }
  }


  public static class ExecuteScript extends SpecialFunc{
    private static final int nparams = 1;
    private static final String funcname = "source";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparams , nparams , args);
      assert args.get(0).getType().equals("String");
    
      Data script_result = new VoidData();
      StringData file = (StringData) args.get(0);
      String infile = file.getValue();
      boolean execute = true;
      
      CharStream input = null;
        try {
            input = new ANTLRFileStream(infile);
        } catch (IOException e) {
            System.err.println ("Error: file " + infile + " could not be opened.");
            System.exit(1);
        }

        // Creates the lexer
        AslLexer lex = new AslLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // Creates and runs the parser. As a result, an AST is created
        AslParser parser = new AslParser(tokens);
        AslTreeAdaptor adaptor = new AslTreeAdaptor();
        parser.setTreeAdaptor(adaptor);
        AslParser.prog_return result = null;
        try {
            result = parser.prog();
        } catch (Exception e) {} // Just catch the exception (nothing to do)
        
        // Check for parsing errors
        int nerrors = parser.getNumberOfSyntaxErrors();
        if (nerrors > 0) {
            System.err.println (nerrors + " errors detected. " +
                                "The program has not been executed.");
            System.exit(1);
        }

        // Get the AST
        AslTree t = (AslTree)result.getTree();

        // Start interpretation (only if execution required)
        if (execute) {
            // Creates and prepares the interpreter
            Interp I = null;
            int linenumber = -1;
            
            try {
                I = new Interp(t, null); // prepares the interpreter
                script_result = I.Run();                  // Executes the code
            } catch (RuntimeException e) {
                if (I != null) linenumber = I.lineNumber();
                System.err.print ("Runtime error");
                if (linenumber < 0) System.err.print (": ");
                else System.err.print (" (" + infile + ", line " + linenumber + "): ");
                System.err.println (e.getMessage() + ".");
                System.err.format (I.getStackTrace());
            } catch (StackOverflowError e) {
                if (I != null) linenumber = I.lineNumber();
                System.err.print("Stack overflow error");
                if (linenumber < 0) System.err.print (".");
                else System.err.println (" (" + infile + ", line " + linenumber + ").");
                System.err.format (I.getStackTrace(5));
            }
        }
        
      return script_result;
    }
  }
  

  public static class Drop extends SpecialFunc{
    private static final int nparamsMin = 1;
    private static final int nparamsMax = 2;
    private static final String funcname = "drop";
    public Data call(ArrayList<Data> args) {
      checkParams(funcname, nparamsMin , nparamsMax , args);
      assert args.get(0).getType().equals("Table");
      TableData t = (TableData) args.get(0);
      if(args.size() > 1){
        String type = args.get(1).getType();
        if(type.equals("Integer")){
          IntegerData row = (IntegerData) args.get(1);
          t.DropRow(row);
          return null;
        }else if(type.equals("String")){
          StringData col = (StringData) args.get(1);
          t.DropColumn(col);
          return null;
        }
        assert false;
      }else{
        //Empty table
        t = new TableData(t.getStringDataLabels());
      }
      return null;
    }
  }
  
  

}
