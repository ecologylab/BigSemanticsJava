package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;

import ecologylab.generic.StringTools;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_collection;

/**
 * Override params using specs in the fragment / after the hash (#). This is used by some website
 * like google image search.
 * 
 * @author quyin
 */
public class OverrideParams extends ElementState
{

  @simpl_collection("name")
  private ArrayList<String> names;

  public OverrideParams()
  {
    // no op
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

}
