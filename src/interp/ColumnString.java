package interp;

public class ColumnString extends ColumnData {

  private StringData value;

  public ColumnString(StringData sd) {
    value = cast(sd.deepClone()).value;
  }

  public Data deepClone() {
    ColumnString res = new ColumnString(value);
    return res;
  }

  public void setValue(Data d) {
    value = ColumnString.cast(d.deepClone()).value;
  }

  public static ColumnString cast(Data data) {
    if (!(data instanceof ColumnString))
      throw new RuntimeException("Received " + data.getType() + ", expected ColumnString\n");
    else
      return (ColumnString) data;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof ColumnString)) {
      return false;
    }
    ColumnString cs = (ColumnString) o;
    return value.equals(cs.value);
  }

}
