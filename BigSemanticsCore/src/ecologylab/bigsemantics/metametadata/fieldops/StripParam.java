package ecologylab.bigsemantics.metametadata.fieldops;

import java.util.HashMap;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
public class StripParam implements FieldOp
{

  @simpl_scalar
  private String name;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
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
      if (params.containsKey(name))
      {
        params.remove(name);
        return purl.updateParams(params);
      }
      else
      {
        return purl;
      }
    }
    return rawValue;
  }

}
