package ecologylab.semantics.metametadata;

import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

/**
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
    return value == null ? "" : value;
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
