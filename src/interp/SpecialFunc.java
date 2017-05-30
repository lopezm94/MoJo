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
      checkParams(funcname, nparams, args);

      TableData result = new TableData();
      String filepath = StringData.cast(args.get(0)).getValue();
      //System.out.println(filepath);
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
      checkParams(funcname, nparams, args);

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
          for (int j=0; j<labels.size(); j++) {
            Data elem =row.get(labels.get(j));
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

}
