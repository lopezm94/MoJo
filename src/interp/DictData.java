package interp;
import parser.*;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class DictData extends Data {

    private HashMap<StringData, Data> dict;

    public DictData() { dict = new HashMap<StringData, Data>(); }
    public DictData(HashMap<StringData, Data> b) { dict = b; }

    public Data get(String key) {
      return get(new StringData(key));
    }
    public Data get(StringData key) {
      Data val = dict.containsKey(key) ? dict.get(key) : new VoidData();
      return val;
    }
    
    public void put(String key, Data data) {
      put(new StringData(key), data);
    }
    public void put(StringData key, Data data) {
      dict.put(key, data.deepClone());
    }

    public int size() {
      return dict.size();
    }

    public boolean empty() {
      return dict.size() == 0;
    }

    public Set<Map.Entry<StringData,Data>> entrySet() {
      return dict.entrySet();
    }

    @Override
    public int hashCode() {
      return dict.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof DictData)) {
        return false;
      }
      DictData dd = (DictData) o;
      return dict.equals(dd.dict);
    }

    public String getType() { return "Dict"; }

    public String toString() {
      return dict.toString();
    }

    public Data deepClone() {
      DictData res = new DictData();
      for (Map.Entry<StringData, Data> entry : dict.entrySet()) {
        res.dict.put(entry.getKey(), entry.getValue().deepClone());
      }
      assert equals(res);
      return res;
    }

    public static DictData cast(Data data) {
      if (!(data instanceof DictData))
        throw new RuntimeException("Received " + data.getType() + ", expected DictData\n");
      else
        return (DictData) data;
    }

    /**
    * Evaluation of expressions with relational operators.
    * @param op Type of operator (token).
    * @return A Boolean data with the value of the expression.
    */
    public BooleanData evaluateRelational (int op, Data data) {
      if ("Dict" != data.getType())
        return new BooleanData(false);

      DictData d = (DictData) data;
      switch (op) {
          case AslLexer.EQUAL:
            return new BooleanData(equals(d));
          case AslLexer.NOT_EQUAL:
            return new BooleanData(!equals(d));
          default: assert false;
      }
      return null;
    }

    //Concatena dos diccionarios
    public DictData evaluateArithmetic (int op, Data data) {    
      assert "Dict".equals(data.getType());
      DictData dict2 = (DictData) data;
      switch (op) {
          case AslLexer.PLUS:
            HashMap<StringData,Data> dict = ((DictData)deepClone()).dict;
            dict.putAll(dict2.dict);
            return new DictData(dict);
          default: assert false;
      }

      return null;

    }
}
