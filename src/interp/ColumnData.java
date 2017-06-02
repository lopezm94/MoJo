package interp;
import parser.*;

public abstract class ColumnData extends Data {

  public abstract Data deepClone();

  public abstract void setValue(Data d);

  public String getType() {
    return "ColumnId";
  }

  /**
  * Evaluation of expressions with relational operators.
  * @param op Type of operator (token)..
  * @return A Boolean data with the value of the expression.
  */
  public BooleanData evaluateRelational (int op, Data data) {
    if ("ColumnId" != data.getType())
      return new BooleanData(false);

    ColumnData d = (ColumnData) data;
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
