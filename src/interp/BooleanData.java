package interp;
import parser.*;

public class BooleanData extends Data {

  private boolean value;

  public BooleanData() { value = false; }
  public BooleanData(boolean b) { value = b; }

  public boolean getValue() { return value; }

  public void setValue(boolean b) { value = b; }

  public void setValue(Data d) {
    BooleanData aux = cast(d);
    value = (boolean) aux.value;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof BooleanData)) {
      return false;
    }
    BooleanData bd = (BooleanData) o;
    return value == bd.value;
  }

  public String getType() { return "Boolean"; }

  public String toString() {
    return Boolean.toString(value);
  }

  public Data deepClone() {
    return new BooleanData(value);
  }

  public static BooleanData cast(Data data) {
    if (!(data instanceof BooleanData))
      throw new RuntimeException("Received " + data.getType() + ", expected BooleanData\n");
    else
      return (BooleanData) data;
  }

  /**
  * Evaluation of expressions with relational operators.
  * @param op Type of operator (token).
  * @return A Boolean data with the value of the expression.
  */
  public BooleanData evaluateRelational (int op, Data data) {
    if ("Boolean" != data.getType())
      return new BooleanData(false);

    BooleanData d = (BooleanData) data;
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
