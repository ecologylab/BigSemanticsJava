package ecologylab.bigsemantics.metametadata.fieldops;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
public class Substring implements FieldOp
{

  @simpl_scalar
  private int    begin;

  @simpl_scalar
  private int    end;

  @simpl_scalar
  private String after;

  @simpl_scalar
  private String before;

  @simpl_scalar
  private String inclusiveAfter;

  @simpl_scalar
  private String inclusiveBefore;

  public int getBegin()
  {
    return begin;
  }

  public void setBegin(int begin)
  {
    this.begin = begin;
  }

  public int getEnd()
  {
    return end;
  }

  public void setEnd(int end)
  {
    this.end = end;
  }

  public String getAfter()
  {
    return after;
  }

  public void setAfter(String after)
  {
    this.after = after;
  }

  public String getBefore()
  {
    return before;
  }

  public void setBefore(String before)
  {
    this.before = before;
  }

  public String getInclusiveAfter()
  {
    return inclusiveAfter;
  }

  public void setInclusiveAfter(String inclusiveAfter)
  {
    this.inclusiveAfter = inclusiveAfter;
  }

  public String getInclusiveBefore()
  {
    return inclusiveBefore;
  }

  public void setInclusiveBefore(String inclusiveBefore)
  {
    this.inclusiveBefore = inclusiveBefore;
  }

  @Override
  public Object operateOn(Object rawValue)
  {
    if (rawValue != null)
    {
      String s = rawValue.toString();
      int a = 0;
      if (after != null)
      {
        int p = s.indexOf(after);
        if (p >= 0)
        {
          a = p + after.length();
        }
      }
      else if (inclusiveAfter != null)
      {
        int p = s.indexOf(inclusiveAfter);
        if (p >= 0)
        {
          a = p;
        }
      }
      else
      {
        a = begin;
      }

      int b = s.length();
      if (before != null)
      {
        int p = s.lastIndexOf(before);
        if (p >= 0)
        {
          b = p;
        }
      }
      else if (inclusiveBefore != null)
      {
        int p = s.lastIndexOf(inclusiveBefore);
        if (p >= 0)
        {
          b = p + inclusiveBefore.length();
        }
      }
      else
      {
        b = (end == 0) ? s.length() : end;
      }

      return s.substring(a, b);
    }
    return rawValue;
  }

}
