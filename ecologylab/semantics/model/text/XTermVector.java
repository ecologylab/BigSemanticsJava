package ecologylab.semantics.model.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XTermVector extends XVector<XTerm> implements ITermVector
{
  
  static Pattern WORD_REGEX = Pattern.compile("[A-z]+(-[A-z]+)*('[A-z]+){0,1}");

	public XTermVector()
	{
	}

	public XTermVector(XTermVector copyMe)
	{
		super(copyMe);
	}

	public XTermVector(int size)
	{
		super(size); // lol
	}

	public XTermVector(String s)
	{
	  reset(s);
	}

	/**
	 * Totally reconstructs this term vector based on a new string. Useful for
	 * maintaining the observers and such while changing the actual terms.
	 * 
	 * @param s
	 */
	public void reset(String s)
	{
	  s=s.toLowerCase();
	  PorterStemmer p = new PorterStemmer();
	  Matcher m = WORD_REGEX.matcher(s);
	  String stem;
	  while (m.find()) {
	    for (int i=m.start(); i<m.end(); i++)
	      p.add(s.charAt(i));
	    p.stem();
	    stem = p.toString();
	    addWithoutNotify(XTermDictionary.getTerm(stem),1);
	  }
	  setChanged();
	  notifyObservers();
	}
	
	private void addWithoutNotify(XTerm term, double val)
	{
	  if (term == null)
	    return;
	  super.add(term, val);
	}

	public void add(XTerm term, double val)
	{
		super.add(term, val);
		setChanged();
		notifyObservers();
	}

	/**
	 * Pairwise multiplies this Vector by another Vector, in-place.
	 * 
	 * @param v
	 *            Vector by which to multiply
	 */
	public void multiply(ITermVector v)
	{
		super.multiply(v);
		setChanged();
		notifyObservers();
	}

	/**
	 * Scalar multiplication of this vector by some constant
	 * 
	 * @param c
	 *            Constant to multiply this vector by.
	 */
	public void multiply(double c)
	{
		super.multiply(c);
		setChanged();
		notifyObservers();
	}

	/**
	 * Pairwise addition of this vector by some other vector times some
	 * constant.<br>
	 * i.e. this + (c*v)<br>
	 * Vector v is not modified.
	 * 
	 * @param c
	 *            Constant which Vector v is multiplied by.
	 * @param v
	 *            Vector to add to this one
	 */
	public void add(double c, ITermVector v)
	{
		super.add(c, v);
		setChanged();
		notifyObservers();
	}

	/**
	 * Adds another Vector to this Vector, in-place.
	 * 
	 * @param v
	 *            Vector to add to this
	 */
	public void add(ITermVector v)
	{
		super.add(v);
		setChanged();
		notifyObservers();
	}
	
	 public String toString()
	  {
	    StringBuilder s = new StringBuilder("{");
	    for(XTerm t : values.keySet())
	      s.append(t.toString() + "(" + values.get(t) + "), ");
	    s.append("}");
	    return s.toString();
	  }
}
