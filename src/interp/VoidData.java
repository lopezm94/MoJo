package interp;
import parser.*;

public class VoidData extends Data {

  public VoidData() { assert true; }

  public String getType() { return "Void"; }

  public String toString() { return "Void"; }

  public void setValue(Data d) {
    throw new RuntimeException("Not possible to set value to data type " + d.getClass());
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof VoidData)) {
      return false;
    }
    return true;
  }

  public Data deepClone() {
    return new VoidData();
  }

  public static VoidData cast(Data data) {
    if (!(data instanceof VoidData))
      throw new RuntimeException("Received " + data.getType() + ", expected VoidData\n");
    else
      return (VoidData) data;
  }

  /**
  * Evaluation of expressions with relational operators.
  * @param op Type of operator (token).
  * @return A Boolean data with the value of the expression.
  */
  public BooleanData evaluateRelational (int op, Data data) {
    switch (op) {
        case AslLexer.EQUAL:
          return new BooleanData(equals(data));
        case AslLexer.NOT_EQUAL:
          return new BooleanData(!equals(data));
        default: assert false;
    }
    return null;
  }
}
