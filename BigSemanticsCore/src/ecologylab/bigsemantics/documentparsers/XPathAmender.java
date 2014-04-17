package ecologylab.bigsemantics.documentparsers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.actions.SemanticsConstants;
import ecologylab.collections.Scope;

/**
 * Amends XPaths for 1) fixing potential problems with extraction, such as converting absolute paths
 * to relative paths, and 2) enhancing XPath functionalities.
 * 
 * This class should be stateless and thread safe, for reusing between threads.
 * 
 * @author quyin
 */
public class XPathAmender implements SemanticsConstants
{

  public static final String LOOP_VAR = "$i";

  static Logger              logger   = LoggerFactory.getLogger(XPathAmender.class);

  public String amend(String xpath, Scope<Object> params)
  {
    String result = xpath;
    if (result != null)
    {
      result = absoluteToRelative(result);
      result = assignLoopVariables(result, params);
      result = joinLines(result);
    }
    if (result != xpath && !result.equals(xpath))
    {
      logger.debug("Amended xpath \"{}\" to \"{}\"", xpath, result);
    }
    return result;
  }

  /**
   * To prevent infinite loop when a type refers to itself, e.g.
   * &lt;google_patent&gt;.&lt;references&gt;
   * 
   * @param xpath
   * @return
   */
  protected String absoluteToRelative(String xpath)
  {
    // in the beginning
    if (xpath.startsWith("/"))
    {
      xpath = "." + xpath;
    }

    // can also be like "(//xpath1) or (//xpath2)".
    if (xpath.contains("(/"))
    {
      xpath = xpath.replace("(/", "(./");
    }

    // TODO with more and more cases, eventually we may need to fully parse xpaths to amend it.

    return xpath;
  }

  protected String assignLoopVariables(String xpath, Scope<Object> params)
  {
    if (xpath.contains(LOOP_VAR))
    {
      int elementIndex = (Integer) params.get(ELEMENT_INDEX_IN_COLLECTION);
      xpath.replace(LOOP_VAR, String.valueOf(elementIndex + 1));
    }
    return xpath;
  }

  protected String joinLines(String xpath)
  {
    if (xpath.contains("\n") || xpath.contains("\r"))
    {
      xpath = xpath.replace("\n", "").replace("\r", "");
    }
    return xpath;
  }

}
