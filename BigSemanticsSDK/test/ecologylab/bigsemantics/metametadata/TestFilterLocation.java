package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import ecologylab.bigsemantics.metametadata.fieldops.DecodeUrl;
import ecologylab.bigsemantics.metametadata.fieldops.GetParam;
import ecologylab.bigsemantics.metametadata.fieldops.OverrideParams;
import ecologylab.net.ParsedURL;

/**
 * 
 * @author quyin
 */
public class TestFilterLocation
{

  @Test
  public void testOverrideParams() throws Exception
  {
    String url = "https://www.google.com/search?tbm=isch&source=hp&biw=1147&bih=1218&q=avatar&oq=avatar#newwindow=1&tbm=isch&q=3d+movies";
    ParsedURL orig = ParsedURL.getAbsolute(url);

    OverrideParams overrideParams = new OverrideParams();
    FilterLocation filter = new FilterLocation();
    filter.addOp(overrideParams);

    ArrayList<ParsedURL> otherLocs = new ArrayList<ParsedURL>();
    ParsedURL filtered = filter.filter(orig, otherLocs);

    HashMap<String, String> params = filtered.extractParams(true);
    Assert.assertEquals("3d movies", params.get("q"));
  }

  @Test
  public void testExtractParam() throws Exception
  {
    String url = "https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=11&cad=rja&uact=8&ved=0CIIBEBYwCg&url=http%3A%2F%2Fexample.com%2Fresult.html%3Fpage%3D2023%26id%3D__id__%23foo&ei=E6JaVOWQA4uHyQTX04KwDg&usg=AFQjCNHWNnajgzcm_JOhsZlx7nVzKDuEgg&sig2=_CDrDhq0QXbcK2zp-xsPUA&bvm=bv.78972154,bs.1,d.cGE";
    ParsedURL orig = ParsedURL.getAbsolute(url);

    GetParam getParam = new GetParam();
    getParam.setName("url");
    DecodeUrl decodeUrl = new DecodeUrl();
    FilterLocation filter = new FilterLocation();
    filter.addOp(getParam);
    filter.addOp(decodeUrl);

    ArrayList<ParsedURL> otherLocs = new ArrayList<ParsedURL>();
    ParsedURL filtered = filter.filter(orig, otherLocs);

    Assert.assertEquals("http://example.com/result.html?page=2023&id=__id__#foo",
                        filtered.toString());
    Assert.assertTrue(otherLocs.contains(orig));
  }

}
