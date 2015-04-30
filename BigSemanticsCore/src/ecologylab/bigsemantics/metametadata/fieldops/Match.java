package ecologylab.bigsemantics.metametadata.fieldops;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
public class Match implements FieldOp
{

  @simpl_scalar
  private Pattern pattern;

  @simpl_scalar
  private int     group = 0;

  @simpl_scalar
  private String  onMatch;

  @simpl_scalar
  private String  onFind;

  @simpl_scalar
  private String  onFail;

  public Pattern getPattern()
  {
    return pattern;
  }

  public void setPattern(Pattern pattern)
  {
    this.pattern = pattern;
  }

  public int getGroup()
  {
    return group;
  }

  public void setGroup(int group)
  {
    this.group = group;
  }

  public String getOnMatch()
  {
    return onMatch;
  }

  public void setOnMatch(String onMatch)
  {
    this.onMatch = onMatch;
  }

  public String getOnFind()
  {
    return onFind;
  }

  public void setOnFind(String onFind)
  {
    this.onFind = onFind;
  }

  public String getOnFail()
  {
    return onFail;
  }

  public void setOnFail(String onFail)
  {
    this.onFail = onFail;
  }

  @Override
  public Object operateOn(Object rawValue)
  {
    if (rawValue != null && pattern != null)
    {
      Matcher matcher = pattern.matcher(rawValue.toString());
      if (onMatch != null && matcher.matches())
      {
        return onMatch;
      }
      if (matcher.find())
      {
        if (onFind != null)
        {
          return onFind;
        }
        else
        {
          return matcher.group(group);
        }
      }
      if (onFail != null)
      {
        return onFail;
      }
    }
    return rawValue;
  }

}
