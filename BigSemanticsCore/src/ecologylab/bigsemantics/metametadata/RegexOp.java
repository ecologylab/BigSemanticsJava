package ecologylab.bigsemantics.metametadata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Operates on extraction results using regular expressions.
 * 
 * @author quyin
 */
@simpl_inherit
public class RegexOp implements FieldOp
{

  static Logger   logger        = LoggerFactory.getLogger(RegexOp.class);

  @simpl_scalar
  private Pattern regex;

  @simpl_scalar
  private int     group;

  @simpl_scalar
  private String  replace;

  @simpl_scalar
  private boolean normalizeText = true;

  @simpl_scalar
  private boolean mustMatch     = true;

  private String  javaRegex;

  private String  javaReplace;

  public RegexOp()
  {
    this(null, null, 0, true, true);
  }

  public RegexOp(Pattern regex, int group)
  {
    this(regex, null, group, true, true);
  }

  public RegexOp(Pattern regex, String replace)
  {
    this(regex, replace, 0, true, true);
  }

  public RegexOp(Pattern regex, String replace, int group, boolean normalizeText, boolean mustMatch)
  {
    super();
    this.regex = regex;
    this.replace = replace;
    this.group = group;
    this.normalizeText = normalizeText;
    this.mustMatch = mustMatch;
  }

  public Pattern getRegex()
  {
    return regex;
  }

  public String getReplace()
  {
    return replace;
  }

  public int getGroup()
  {
    return group;
  }

  public boolean isNormalizeText()
  {
    return normalizeText;
  }

  public boolean isMustMatch()
  {
    return mustMatch;
  }

  public String getJavaRegex()
  {
    if (javaRegex == null && regex != null)
      javaRegex = regex.pattern().replaceAll("\\\\", "\\\\\\\\");
    return javaRegex;
  }

  public String getJavaReplace()
  {
    if (javaReplace == null && replace != null)
      javaReplace = replace.replaceAll("\\\\", "\\\\\\\\");
    return javaReplace;
  }

  @Override
  public String operateOn(String rawValue)
  {
    if (rawValue == null)
    {
      return null;
    }

    String result = rawValue;

    result = normalizeIfNeeded(result);

    if (regex != null)
    {
      Matcher matcher = regex.matcher(result);
      if (matcher.find())
      {
        if (replace == null)
        {
          // find and extract.
          if (group >= 0 && group <= matcher.groupCount())
          {
            result = matcher.group(group);
            logger.info("Pattern found: regex={}, result={}", regex, result);
          }
          else
          {
            logger.warn("Pattern not found: string={}, regex={}, group={}", result, regex, group);
          }
        }
        else
        {
          result = matcher.replaceAll(replace);
          logger.info("Pattern replaced: regex={}, result={}", regex, result);
        }
      }
      else
      {
        logger.warn("Pattern not found: string={}, regex={}, group={}", result, regex, group);
        if (mustMatch)
        {
          result = "";
          logger.warn("Reset to empty.");
        }
      }
    }

    result = normalizeIfNeeded(result);

    return result;
  }

  private String normalizeIfNeeded(String text)
  {
    if (isNormalizeText())
    {
      text = text.replaceAll("\\s+", " ").trim();
      text = text.replaceAll("\u00A0", " ").trim(); // &nbsp;
      logger.info("Extraction result normalized to {}", text);
    }
    return text;
  }

}
