package ecologylab.bigsemantics.metametadata.fieldops;

import java.util.HashMap;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 *
 * @author quyin
 */
public class SetParam implements FieldOp
{

  @simpl_scalar
  private String  name;

  @simpl_scalar
  private String  value;

  @simpl_scalar
  private boolean onlyWhenNotSet;

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
  public Object operateOn(Object rawValue)
  {
    if (rawValue != null && name != null && value != null)
    {
      ParsedURL purl = (rawValue instanceof ParsedURL)
          ? ((ParsedURL) rawValue)
          : ParsedURL.getAbsolute(rawValue.toString());
      HashMap<String, String> params = purl.extractParams(true);
      if (params == null) params = new HashMap<String, String>();
      if (!onlyWhenNotSet || !params.containsKey(name))
      {
        params.put(name, value);
      }
      return purl.updateParams(params);
    }
    return rawValue;
  }

}
