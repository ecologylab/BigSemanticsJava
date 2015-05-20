package ecologylab.bigsemantics.metametadata.fieldops;

import java.util.HashMap;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
public class GetParam implements FieldOp
{

  @simpl_scalar
  private String name;

  @simpl_scalar
  private String otherwise;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getOtherwise()
  {
    return otherwise;
  }

  public void setOtherwise(String otherwise)
  {
    this.otherwise = otherwise;
  }

  @Override
  public Object operateOn(Object rawValue)
  {
    if (rawValue != null && name != null)
    {
      ParsedURL purl = (rawValue instanceof ParsedURL)
          ? ((ParsedURL) rawValue)
          : ParsedURL.getAbsolute(rawValue.toString());
      HashMap<String, String> params = purl.extractParams(true);
      if (params != null && params.containsKey(name))
      {
        return params.get(name);
      }
      else
      {
        return otherwise;
      }
    }
    return rawValue;
  }

}
