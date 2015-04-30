package ecologylab.bigsemantics.metametadata.fieldops;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
public class Replace implements FieldOp
{

  @simpl_scalar
  private Pattern pattern;

  @simpl_scalar
  private String  to = "";

  @simpl_scalar
  private boolean firstOnly;

  public Pattern getPattern()
  {
    return pattern;
  }

  public void setPattern(Pattern pattern)
  {
    this.pattern = pattern;
  }

  public String getTo()
  {
    return to;
  }

  public void setTo(String to)
  {
    this.to = to;
  }

  public boolean isFirstOnly()
  {
    return firstOnly;
  }

  public void setFirstOnly(boolean firstOnly)
  {
    this.firstOnly = firstOnly;
  }

  @Override
  public Object operateOn(Object rawValue)
  {
    if (rawValue != null && pattern != null)
    {
      Matcher matcher = pattern.matcher(rawValue.toString());
      if (firstOnly)
      {
        return matcher.replaceFirst(to);
      }
      else
      {
        return matcher.replaceAll(to);
      }
    }
    return rawValue;
  }

}
