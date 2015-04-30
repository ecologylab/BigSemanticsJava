package ecologylab.bigsemantics.metametadata.fieldops;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
public class Strip implements FieldOp
{

  @simpl_scalar
  private String anyOf;

  public String getAnyOf()
  {
    return anyOf;
  }

  public void setAnyOf(String anyOf)
  {
    this.anyOf = anyOf;
  }

  private boolean containsAny(String s, char c)
  {
    return s.indexOf(c) >= 0;
  }

  @Override
  public Object operateOn(Object rawValue)
  {
    if (rawValue != null)
    {
      String s = rawValue.toString();
      if (anyOf == null)
      {
        return s.trim();
      }
      else
      {
        int a = 0, b = s.length() - 1;
        while (a <= b && containsAny(anyOf, s.charAt(a)))
        {
          a++;
        }
        while (b >= a && containsAny(anyOf, s.charAt(b)))
        {
          b--;
        }
        return (a <= b) ? s.substring(a, b + 1) : "";
      }
    }
    return rawValue;
  }
}
