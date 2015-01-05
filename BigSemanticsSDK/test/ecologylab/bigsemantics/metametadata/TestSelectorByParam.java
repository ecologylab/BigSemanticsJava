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

  /**
   * This test case verifies a use case of MetaMetadataSelectorParam:
   * <p>
   * &lt;param name="name" /&gt;
   * <p>
   * It should match URLs with a parameter "name" which has non-empty values, by return true for
   * checkForParams(). Otherwise the method returns false.
   */
  @Test
  public void testRequiringParamNameWithNonEmptyValue()
  {
    // use a <param> object that matches parameter by name, and the parameter value must be
    // non-empty.
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

  /**
   * This test case verifies a use case of MetaMetadataSelectorParam:
   * <p>
   * &lt;param name="name" value="value" /&gt;
   * <p>
   * It should match URLs with a parameter "name" which has a specific value "value", by return true
   * for checkForParams(). Otherwise the method returns false.
   */
  @Test
  public void testRequiringParamNameAndSpecificValue()
  {
    // use a <param> object that matches parameter by name as well as value.
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

  /**
   * This test case verifies a use case of MetaMetadataSelectorParam:
   * <p>
   * &lt;param name="name" allow_empty_value="true" /&gt;
   * <p>
   * It should match URLs with a parameter "name" whose value can be empty or not, by return true
   * for checkForParams(). Otherwise the method returns false.
   */
  @Test
  public void testRequiringParamNameExists()
  {
    // use a <param> object that matches parameter by name, and no requirements on the parameter's
    // value.
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
