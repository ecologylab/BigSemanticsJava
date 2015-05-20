package ecologylab.bigsemantics.metametadata.fieldops;

import java.util.ArrayList;
import java.util.HashMap;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_nowrap;

/**
 * 
 * @author quyin
 */
public class StripParamsBut implements FieldOp
{

  @simpl_collection("name")
  @simpl_nowrap
  private ArrayList<String> names;

  public ArrayList<String> getNames()
  {
    return names;
  }

  public void setNames(ArrayList<String> names)
  {
    this.names = names;
  }

  public void addName(String name)
  {
    if (names == null)
    {
      names = new ArrayList<String>();
    }
    names.add(name);
  }

  @Override
  public Object operateOn(Object rawValue)
  {
    if (rawValue != null)
    {
      ParsedURL purl = (rawValue instanceof ParsedURL)
          ? ((ParsedURL) rawValue)
          : ParsedURL.getAbsolute(rawValue.toString());
      HashMap<String, String> params = purl.extractParams(true);
      HashMap<String, String> newParams = new HashMap<String, String>();
      if (names != null)
      {
        for (String name : names)
        {
          if (params.containsKey(name))
          {
            newParams.put(name, params.get(name));
          }
        }
      }
      return purl.updateParams(newParams);
    }
    return rawValue;
  }

}
