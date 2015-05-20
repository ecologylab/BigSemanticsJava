package ecologylab.bigsemantics.metametadata.fieldops;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
public class Append implements FieldOp
{

  @simpl_scalar
  private String value;

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  @Override
  public Object operateOn(Object rawValue)
  {
    return rawValue == null ? value : (rawValue.toString() + value);
  }

}
