package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import ecologylab.net.ParsedURL;

/**
 * 
 * @author quyin
 */
public class TestFilterLocation
{

  @Test
  public void testOverrideParams()
  {
    String url = "https://www.google.com/search?tbm=isch&source=hp&biw=1147&bih=1218&q=avatar&oq=avatar#newwindow=1&tbm=isch&q=3d+movies";
    ParsedURL orig = ParsedURL.getAbsoluteWithFragment(url);
    
    OverrideParams overrideParams = new OverrideParams();
    FilterLocation filter = new FilterLocation();
    filter.setOverrideParams(overrideParams);
    
    ArrayList<ParsedURL> otherLocs = new ArrayList<ParsedURL>();
    ParsedURL filtered = filter.filter(orig, otherLocs);
    
    HashMap<String, String> params = filtered.extractParams(true);
    Assert.assertEquals("3d movies", params.get("q"));
  }

}
