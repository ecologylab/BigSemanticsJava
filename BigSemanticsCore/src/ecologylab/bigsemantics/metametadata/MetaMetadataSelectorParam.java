package ecologylab.bigsemantics.metametadata;

import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

/**
 * Matching URL parameters in selectors.
 * <p>
 * Usage:
 * <pre>
 * &lt;selector url_stripped=...&gt
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;param name=... value=... /&gt;
 * &lt;/selector&gt;
 * </pre>
 * Name is required. When value is set, the actual parameter value must be the same as this value to
 * match. When value is not set, it only requires that the URL contains this parameter.
 * <p>
 * Currently, This only supports url_stripped and url_regex.
 * 
 * @author quyin
 * 
 */
public class MetaMetadataSelectorParam implements IMappable<String>
{

  @simpl_scalar
  private String name;

  @simpl_scalar
  private String value;

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
