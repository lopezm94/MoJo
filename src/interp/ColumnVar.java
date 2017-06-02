package interp;

public class ColumnVar extends ColumnData {

  private Data value;

  public ColumnVar(Data d) {
    value = d;
  }

  public Data deepClone() {
    ColumnVar res = new ColumnVar(value);
    return res;
  }

  public void setValue(Data d) {
    value = cast(d).value;
  }

  public static ColumnVar cast(Data data) {
    if (!(data instanceof ColumnVar))
      throw new RuntimeException("Received " + data.getType() + ", expected ColumnVar\n");
    else
      return (ColumnVar) data;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof ColumnVar)) {
      return false;
    }
    ColumnVar cv = (ColumnVar) o;
    return value.equals(cv.value);
  }

}
