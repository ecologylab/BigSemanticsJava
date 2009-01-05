package ecologylab.semantics.model.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.generic.VectorType;

public class XTermVector extends XVector<XTerm>
{

	static Pattern WORD_REGEX = Pattern.compile("[a-z]+(-[a-z]+)*([a-z]+)");

	public XTermVector()
	{
	}

	public XTermVector(VectorType<XTerm> tv)
	{
		super(tv);
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
		Matcher m 			= WORD_REGEX.matcher(s);
		while (m.find()) {
			String word = s.substring(m.start(),m.end());
			addWithoutNotify(XTermDictionary.getTermForWord(word),1);
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
	public void multiply(VectorType<XTerm> v)
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
	public void add(double c, VectorType<XTerm> v)
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
	public void add(VectorType<XTerm> v)
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

	@Override
	public double idfDot(VectorType<XTerm> v)
	{
		HashMap<XTerm,Double> other = v.map();
		if (other == null || this.norm() == 0 || v.norm() == 0)
			return 0;

		double dot = 0;
		HashMap<XTerm, Double> vector = this.values;
		synchronized(values) {
			for (XTerm term : vector.keySet()) {
				if (other.containsKey(term)) {
					dot += vector.get(term) * term.idf() * v.get(term);
				}
			}
		}
		return dot;
	}

	private double magicalScalingFactor(double d)
	{
		return Math.exp(d);
	}
	
	public void clamp(double clampTo)
	{
		super.clamp(clampTo);
		setChanged();
		notifyObservers();
	}
	
	public void clampExp(double clampTo)
	{
		super.clampExp(clampTo);
		setChanged();
		notifyObservers();
	}
	
	@Override
	public XTermVector unit()
	{
		XTermVector v = new XTermVector(this);
		v.clamp(1);
		return v;
	}
	
	@Override
	public XTermVector simplex ( )
	{
		XTermVector v = new XTermVector(this);
		for (XTerm t : v.values.keySet())
		{
			v.values.put(t, 1.0);
		}
		return v;
	}
	
	public void trim(int size)
	{
		TreeMap<Double,XTerm> highestWeightedTerms = new TreeMap<Double,XTerm>();
		for (XTerm t : values.keySet())
		{
			highestWeightedTerms.put(t.idf(), t);
		}
		synchronized (values)
		{
			for(Double d : highestWeightedTerms.keySet())
			{
				if (values.size() <= size)
					break;
				values.remove(highestWeightedTerms.get(d));
			}			
		}
	}
	
}
