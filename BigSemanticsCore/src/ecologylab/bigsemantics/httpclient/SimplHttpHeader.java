package ecologylab.bigsemantics.httpclient;

import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

/**
 * HTTP header.
 * 
 * @author quyin
 */
public class SimplHttpHeader implements IMappable<String>
{

  @simpl_scalar
  private String name;

  @simpl_scalar
  private String value;

  public SimplHttpHeader()
  {
    this(null, null);
  }

  public SimplHttpHeader(String name, String value)
  {
    super();
    this.name = name;
    this.value = value;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  @Override
  public String key()
  {
    return name;
  }

}
