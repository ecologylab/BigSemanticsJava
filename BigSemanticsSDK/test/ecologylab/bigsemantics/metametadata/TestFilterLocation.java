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
  
  @Test
  public void testExtractParam()
  {
    String url = "https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=11&cad=rja&uact=8&ved=0CIIBEBYwCg&url=http%3A%2F%2Fexample.com%2Fresult.html%3Fpage%3D2023%26id%3D__id__%23foo&ei=E6JaVOWQA4uHyQTX04KwDg&usg=AFQjCNHWNnajgzcm_JOhsZlx7nVzKDuEgg&sig2=_CDrDhq0QXbcK2zp-xsPUA&bvm=bv.78972154,bs.1,d.cGE";
    
    FilterLocation filter = new FilterLocation();
    filter.setExtractParam("url");
    filter.setDecodeUrl(true);
    
    String result = filter.operateOn(url);
    
    Assert.assertEquals("http://example.com/result.html?page=2023&id=__id__#foo", result);
  }

}
