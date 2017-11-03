package ecologylab.bigsemantics.metametadata;

import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

/**
 * Matching URL parameters in selectors.
 * <p>
 * Usage:
 * 
 * <pre>
 * &lt;selector url_stripped=...&gt
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;param name=... value=... allow_empty_value=... /&gt;
 * &lt;/selector&gt;
 * </pre>
 * 
 * Name is required. When value is set, the actual parameter value must be the same as this value to
 * match. When value is not set, it looks at the boolean attribute allow_empty_value: if
 * allow_empty_value is false, it requires that the URL contains this parameter and its value be
 * non-empty; if allow_empty_value is true, it only requires that the URL contains this parameter,
 * with empty or non-empty values.
 * <p>
 * Currently, This only supports url_stripped and url_regex.
 * 
 * @author quyin
 * 
 */
public class MetaMetadataSelectorParam implements IMappable<String>
{

  @simpl_scalar
  private String  name;

  @simpl_scalar
  private String  value;
  
  @simpl_scalar
  private String  valueIsNot;

  @simpl_scalar
  private boolean allowEmptyValue;

  @simpl_scalar
  private boolean mustNotValued;

  public MetaMetadataSelectorParam()
  {
    this(null);
  }

  MetaMetadataSelectorParam(String name)
  {
    this(name, null);
  }

  MetaMetadataSelectorParam(String name, String value)
  {
    this(name, value, false);
  }

  /**
   * @param name
   * @param value
   * @param allowEmptyValue
   */
  MetaMetadataSelectorParam(String name, String value, boolean allowEmptyValue)
  {
    super();
    this.name = name;
    this.value = value;
    this.allowEmptyValue = allowEmptyValue;
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

  public String getValueIsNot()
  {
    return valueIsNot;
  }

  public void setValueIsNot(String valueIsNot)
  {
    this.valueIsNot = valueIsNot;
  }

  public boolean isAllowEmptyValue()
  {
    return allowEmptyValue;
  }

  public void setAllowEmptyValue(boolean allowEmptyValue)
  {
    this.allowEmptyValue = allowEmptyValue;
  }

  public boolean isMustNotValued()
  {
    return mustNotValued;
  }

  public void setMustNotValued(boolean mustNotValued)
  {
    this.mustNotValued = mustNotValued;
  }

  @Override
  public String key()
  {
    return name;
  }

}
