package interp;
import parser.*;

public class StringData extends Data {

  private String text;

  public StringData() { text = ""; }
  public StringData(String b) { text = b; }

  public String getValue() { return text; }

  public void setValue(String b) { text = b; }

  public String getType() { return "String"; }

  public String toString() {
    return "'" + text + "'";
  }

  public Data deepClone() {
    return new StringData(text);
  }

  @Override
  public int hashCode() {
    return text.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof StringData)) {
      return false;
    }
    StringData s = (StringData) o;
    return text.equals(s.text);
  }

  public static StringData cast(Data data) {
    if (!(data instanceof StringData))
      throw new RuntimeException("Received " + data.getType() + ", expected StringData\n");
    else
      return (StringData) data;
  }

  /**
  * Evaluation of expressions with relational operators.
  * @param op Type of operator (token).
  * @param d Second operand.
  * @return A Boolean data with the text of the expression.
  */
  public BooleanData evaluateRelational(int op, Data data) {
    if ("String" != data.getType())
      return new BooleanData(false);

    StringData d = (StringData) data;
    switch (op) {
        case AslLexer.EQUAL:
          return new BooleanData(equals(d));
        case AslLexer.NOT_EQUAL:
          return new BooleanData(!equals(d));
        default: assert false;
    }
    return null;
  }

    public StringData evaluateArithmetic (int op, Data data) {
      assert "String" != data.getType();

      StringData string2 = (StringData) data;
      switch (op) {
          case AslLexer.PLUS:
            //String concatenation
            return new StringData(text.concat(string2.getValue()));
          default: assert false;
      }

      return null;

    }

}
