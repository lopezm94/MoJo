package interp;
import parser.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;


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
    public TableData(ListData<StringData> ld, ArrayList<String> t) {
      labels = (ListData<StringData>) ld.deepClone();
      types = new ArrayList<String>();
      for (int i=0; i<t.size(); i++) { types.add(t.get(i)); }
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

    public ArrayList<String> getLabels() {
      ArrayList<String> res = new ArrayList<String>();
      for (int i=0; i<width(); i++) {
        res.add(labels.get(i).getValue());
      }
      return res;
    }

    public Data deepClone() {
      TableData res = new TableData(labels, types);
      for (DictData row: table) {
        DictData row_copy = new DictData();
        for (Map.Entry<StringData, Data> entry : row.entrySet()) {
          row_copy.put(entry.getKey(), entry.getValue().deepClone());
        }
        res.table.add(row_copy);
      }
      assert equals(res);
      return res;
    }

    public void merge(TableData table) {
      if (!table.labels.equals(labels))
        throw new RuntimeException("Labels must be equal and in the " +
          "exact same order to be able to merge"
          );
      for (int i=0; i<table.height(); i++) {
        addRow(table.get(i));
      }
    }

    public TableData sort() {
      TableData res = TableData.cast(deepClone());
      Collections.shuffle(res.table);
      return res;
    }

    public TableData sample(IntegerData n) {
      return sample(n.getValue());
    }
    public TableData sample(int n) {
      if (height() < n)
        throw new RuntimeException(
          "Table height "+height()+" is lower than requested sample size "+n+"\n"
          );
      TableData tmp = TableData.cast(deepClone());
      Collections.shuffle(tmp.table);
      TableData res = new TableData(tmp.labels,tmp.types);
      for (int i=0; i<n; i++) {
        res.addRow(tmp.get(i));
      }
      return res;
    }

    public static TableData cast(Data data) {
      if (!(data instanceof TableData))
        throw new RuntimeException("Received " + data.getType() + ", expected TableData\n");
      else
        return (TableData) data;
    }

    /**
    * Evaluation of expressions with relational operators.
    * @param op Type of operator (token)..
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

    public DictData get(int row) {
      return table.get(row);
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
      return get(row).get(col);
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
      table.get(row).put(col, data.deepClone());
    }

    /**Adds a row in the table**/
    public void addRow(){
      table.add(new DictData());
    }
    public void addRow(DictData dd){
      table.add(DictData.cast(dd.deepClone()));
    }

    /**Returns a table with an added row in the table**/
    public TableData addRowCopy(){
      TableData td = (TableData) deepClone();
      td.addRow(new DictData());
      return td;
    }
    public TableData addRowCopy(DictData dd){
      TableData td = (TableData) deepClone();
      td.addRow(dd);
      return td;
    }

    /**Adds a column in the table**/
    public void addColumn(StringData col){
      int index = labels.indexOf(col);
      String type = "";
      if (index < 0) {
        index = width();
        labels.add(StringData.cast(col.deepClone()));
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
        index = width();
        labels.add(StringData.cast(col.deepClone()));
        types.add(type);
      } else {
        types.add(index, type);
      }
      assert height() == ld.size();
      for (int i=0; i<height(); i++) {
          table.get(i).put(col, ld.get(i).deepClone());
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
