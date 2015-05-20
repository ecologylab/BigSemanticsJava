package ecologylab.bigsemantics.metametadata.declarations;

import java.util.ArrayList;
import java.util.List;

import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
public class XPathFor
{

  @simpl_scalar
  private String       context;

  @simpl_collection("xpath")
  @simpl_nowrap
  private List<String> xpaths;

  public String getContext()
  {
    return context;
  }

  public void setContext(String context)
  {
    this.context = context;
  }

  public List<String> getXpaths()
  {
    return xpaths;
  }

  public void setXpaths(List<String> xpaths)
  {
    this.xpaths = xpaths;
  }

  public void addXpath(String xpath)
  {
    if (xpaths == null)
    {
      xpaths = new ArrayList<String>();
    }
    xpaths.add(xpath);
  }

}
