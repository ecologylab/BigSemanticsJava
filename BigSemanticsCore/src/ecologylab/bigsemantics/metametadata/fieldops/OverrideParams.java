package ecologylab.bigsemantics.metametadata.fieldops;

import java.util.ArrayList;
import java.util.HashMap;

import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_collection;

/**
 * 
 * @author quyin
 */
public class OverrideParams implements FieldOp
{

  @simpl_collection("name")
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

  public void overrideParams(HashMap<String, String> params, String fragment)
  {
    if (fragment != null && fragment.length() > 0 && params != null)
    {
      HashMap<String, String> newParams = StringTools.doubleSplit(fragment);
      if (names != null && names.size() > 0)
      {
        for (String name : names)
        {
          params.put(name, newParams.get(name));
        }
      }
      else
      {
        params.putAll(newParams);
      }
    }
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
      overrideParams(params, purl.fragment());
      return purl.updateParams(params);
    }
    return rawValue;
  }

}
