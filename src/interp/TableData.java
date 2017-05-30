package interp;
import parser.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class TableData extends Data {

    private ArrayList<String> types;
    private ListData<StringData> labels;
    private ArrayList<DictData> table;

    public TableData() {
      types = new ArrayList<String>();
      labels = new ListData<StringData>();
      table = new ArrayList<DictData>();
    }
    public TableData(ListData<StringData> ld) {
      labels = (ListData<StringData>) ld.deepClone();
      types = new ArrayList<String>();
      table = new ArrayList<DictData>();
    }

    public int height() {
      return table.size();
    }

    public int width() {
      return labels.size();
    }

    public boolean empty() {
      return labels.size() == 0;
    }

    @Override
    public int hashCode() {
      return table.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof TableData)) {
        return false;
      }
      TableData td = (TableData) o;
      return table.equals(td.table)
        && labels.equals(td.labels)
        && types.equals(td.types);
    }

    public String getType() {
      return "Table";
    }

    public String toString() {
      return table.toString();
    }

    public Data deepClone() {
      TableData res = new TableData();
      for (DictData row: table) {
        DictData row_copy = new DictData();
        for (Map.Entry<StringData, Data> entry : row.entrySet()) {
          row_copy.put(entry.getKey(), entry.getValue().deepClone());
        }
        res.table.add(row_copy);
      }
      return res;
    }

    /**
    * Evaluation of expressions with relational operators.
    * @param op Type of operator (token).
    * @param d Second operand.
    * @return A Boolean data with the value of the expression.
    */
    public BooleanData evaluateRelational (int op, Data data) {
      if ("Table" != data.getType())
        return new BooleanData(false);

      TableData d = (TableData) data;
      switch (op) {
          case AslLexer.EQUAL:
            return new BooleanData(equals(d));
          case AslLexer.NOT_EQUAL:
            return new BooleanData(!equals(d));
          default: assert false;
      }
      return null;
    }

    public Data get(int row, String col) {
      return get(row, new StringData(col));
    }
    
    public Data get(int row, StringData col) {
      if (height() <= row)
        throw new RuntimeException("Index out of bounds: " +
          Integer.toString(height()));
      if (!labels.contains(col))
        throw new RuntimeException("Column name: " + col + " doesn't exist");
      return table.get(row).get(col);
    }

    public ListData<StringData> getColumnNames(){
        return labels;
    }

    
    public void put(int row, String col, Data data) {
      put(row, new StringData(col), data);
    }
    public void put(int row, StringData col, Data data) {
      while (height() <= row)
        addRow();
      table.get(row).put(col, data);
    }

    /**Adds a row in the table**/
    public void addRow(){
      table.add(new DictData());
    }
    public void addRow(DictData dd){
      table.add(dd);
    }

    /**Returns a table with an added row in the table**/
    public TableData addRowCopy(){
      TableData td = new TableData();
      td.addRow(new DictData());
      return td;
    }
    public TableData addRowCopy(DictData dd){
      TableData td = new TableData();
      td.addRow(dd);
      return td;
    }

    /**Adds a column in the table**/
    public void addColumn(StringData col){
      int index = labels.indexOf(col);
      String type = "";
      if (index < 0) {
        index = labels.size();
        labels.add(col);
        types.add(type);
      } else {
        types.add(index, type);
      }
    }
    public void addColumn(StringData col, ListData<Data> ld){
      int index = labels.indexOf(col);
      String type = "";
      if (!ld.empty())
        type = ld.get(0).getType();
      else
        type = "Untyped";
      if (index < 0) {
        index = labels.size();
        labels.add(col);
        types.add(type);
      } else {
        types.add(index, type);
      }
      assert height() == ld.size();
      for (int i=0; i<table.size(); i++) {
          table.get(i).put(col, ld.get(i));
      }
    }
    
    /**Returns a table with an added column in the table**/
    public TableData addColumnCopy(StringData col){
      TableData td = (TableData) deepClone();
      td.addColumn(col);
      return td;
    }
    public TableData addColumnCopy(StringData col, ListData<Data> ld){
      TableData td = (TableData) deepClone();
      td.addColumn(col, ld);
      return td;
    }
}
