package interp;
import parser.*;

public class IntegerData extends Data {

  private int value;

  public IntegerData() { value = 0; }
  public IntegerData(int b) { value = b; }

  public int getValue() { return value; }

  public void setValue(int b) { value = b; }

  public String getType() { return "Integer"; }

  public String toString() {
    return Integer.toString(value);
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof IntegerData)) {
      return false;
    }
    IntegerData bd = (IntegerData) o;
    return value == bd.value;
  }

  public Data deepClone() {
    return new IntegerData(value);
  }

  public static IntegerData cast(Data data) {
    if (!(data instanceof IntegerData))
      throw new RuntimeException("Received " + data.getType() + ", expected IntegerData\n");
    else
      return (IntegerData) data;
  }

  /**
  * Evaluation of expressions with relational operators.
  * @param op Type of operator (token).
  * @return A Boolean data with the value of the expression.
  */
  public BooleanData evaluateRelational(int op, Data data) {
    if ("Integer" != data.getType())
      return new BooleanData(false);

    IntegerData d = (IntegerData) data;
    switch (op) {
        case AslLexer.EQUAL:
          return new BooleanData(equals(d));
        case AslLexer.NOT_EQUAL:
          return new BooleanData(!equals(d));
        case AslLexer.LT:
          return new BooleanData(value < d.value);
        case AslLexer.LE:
          return new BooleanData(value <= d.value);
        case AslLexer.GT:
          return new BooleanData(value > d.value);
        case AslLexer.GE:
          return new BooleanData(value >= d.value);
        default: assert false;
    }
    return null;
  }

  /**
  * Checks for zero (for division). It raises an exception in case
  * the value is zero.
  */
  private void checkDivZero(IntegerData d) {
    if (d.value == 0) throw new RuntimeException ("Division by zero");
  }

  /**
  * Evaluation of arithmetic expressions. The evaluation is done
  * "in place", returning the result on the same data.
  * @param op Type of operator (token).
  * @param d Second operand.value == 1
  * @return A Boolean data with the value of the expression.
  */
  public IntegerData evaluateArithmetic(int op, IntegerData d) {
    switch (op) {
        case AslLexer.PLUS:
          return new IntegerData(value + d.value);
        case AslLexer.MINUS:
          return new IntegerData(value - d.value);
        case AslLexer.MUL:
          return new IntegerData(value * d.value);
        case AslLexer.DIV:
          checkDivZero(d);
          return new IntegerData(value / d.value);
        case AslLexer.MOD:
          checkDivZero(d);
          return new IntegerData(value % d.value);
        default: assert false;
    }
    return null;
  }
}
