package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ecologylab.net.ParsedURL;

public class TestSelectorByParam
{

  MetaMetadataSelector selector;

  @Before
  public void init()
  {
    selector = new MetaMetadataSelector();
  }

  @Test
  public void testRequiringParamNameWithNonEmptyValue()
  {
    selector.addParam(new MetaMetadataSelectorParam("name"));

    ParsedURL purl1 = ParsedURL.getAbsolute("http://example.com/index.html");
    assertFalse(selector.checkForParams(purl1));

    ParsedURL purl2 = ParsedURL.getAbsolute("http://example.com/index.html?name=");
    assertFalse(selector.checkForParams(purl2));

    ParsedURL purl3 = ParsedURL.getAbsolute("http://example.com/index.html?name=value");
    assertTrue(selector.checkForParams(purl3));

    ParsedURL purl4 = ParsedURL.getAbsolute("http://example.com/index.html?type=T");
    assertFalse(selector.checkForParams(purl4));
    
    ParsedURL purl5 = ParsedURL.getAbsolute("http://example.com/index.html?name=other");
    assertTrue(selector.checkForParams(purl5));
  }

  @Test
  public void testRequiringParamNameAndSpecificValue()
  {
    selector.addParam(new MetaMetadataSelectorParam("name", "value"));

    ParsedURL purl1 = ParsedURL.getAbsolute("http://example.com/index.html");
    assertFalse(selector.checkForParams(purl1));

    ParsedURL purl2 = ParsedURL.getAbsolute("http://example.com/index.html?name=");
    assertFalse(selector.checkForParams(purl2));

    ParsedURL purl3 = ParsedURL.getAbsolute("http://example.com/index.html?name=value");
    assertTrue(selector.checkForParams(purl3));

    ParsedURL purl4 = ParsedURL.getAbsolute("http://example.com/index.html?type=T");
    assertFalse(selector.checkForParams(purl4));
    
    ParsedURL purl5 = ParsedURL.getAbsolute("http://example.com/index.html?name=other");
    assertFalse(selector.checkForParams(purl5));
  }
    
  @Test
  public void testRequiringParamNameExists()
  {
    selector.addParam(new MetaMetadataSelectorParam("name", null, true));

    ParsedURL purl1 = ParsedURL.getAbsolute("http://example.com/index.html");
    assertFalse(selector.checkForParams(purl1));

    ParsedURL purl2 = ParsedURL.getAbsolute("http://example.com/index.html?name=");
    assertTrue(selector.checkForParams(purl2));

    ParsedURL purl3 = ParsedURL.getAbsolute("http://example.com/index.html?name=value");
    assertTrue(selector.checkForParams(purl3));

    ParsedURL purl4 = ParsedURL.getAbsolute("http://example.com/index.html?type=T");
    assertFalse(selector.checkForParams(purl4));
    
    ParsedURL purl5 = ParsedURL.getAbsolute("http://example.com/index.html?name=other");
    assertTrue(selector.checkForParams(purl5));
  }

}
