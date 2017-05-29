package interp;
import parser.*;
import java.util.ArrayList;

public class ListData<T extends Data> implements Data {

    private ArrayList<T> list;

    public ListData() { list = new ArrayList<T>(); }
    public ListData(ArrayList<T> b) {
      list = b;
    }

    public void add(T data) {
      list.add(data);
    }
    public void add(int i, T data) {
      list.add(i, data);
    }

    public T get(int i) {
      return list.get(i);
    }

    @Override
    public int hashCode() {
      return list.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof ListData)) {
        return false;
      }
      ListData ld = (ListData) o;
      return list.equals(ld.list);
    }

    public int size() {
      return list.size();
    }

    public boolean empty() {
      return list.size() == 0;
    }

    public int indexOf(Object obj) {
      return list.indexOf(obj);
    }

    public ArrayList<T> toArrayList() {
      return ((ListData<T>)deepClone()).list;
    }

    public String getType() { return "List"; }

    public String toString() {
      return list.toString();
    }

    public Data deepClone() {
      ListData<T> res = new ListData<T>();
      for (T data: list) {
        res.list.add((T) data.deepClone());
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
      if ("List" != data.getType())
        return new BooleanData(false);

      ListData<T> d = (ListData<T>) data;
      switch (op) {
          case AslLexer.EQUAL:
            return new BooleanData(equals(d));
          case AslLexer.NOT_EQUAL:
            return new BooleanData(!equals(d));
          default: assert false;
      }
      return null;
    }
}
