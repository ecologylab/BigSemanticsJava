package ecologylab.semantics.model.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.generic.VectorType;

public class TermVector extends FeatureVector<Term>
{

	private static final String	SHOW_WEIGHTS_PREF	= "show_weights";
	static Pattern WORD_REGEX = Pattern.compile("[a-z]+(-[a-z]+)*([a-z]+)");

	public TermVector()
	{
	}

	public TermVector(VectorType<Term> tv)
	{
		super(tv);
	}

	public TermVector(int size)
	{
		super(size); // lol
	}

	public TermVector(String s)
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
			addWithoutNotify(TermDictionary.getTermForWord(word),1);
		}
		setChanged();
		notifyObservers();
	}

	private void addWithoutNotify(Term term, double val)
	{
		if (term == null || term.isStopword())
			return;
		super.add(term, val);
	}

	public void add(Term term, double val)
	{
		if(!term.isStopword())
		{
			super.add(term, val);
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Pairwise multiplies this Vector by another Vector, in-place.
	 * 
	 * @param v
	 *            Vector by which to multiply
	 */
	public void multiply(VectorType<Term> v)
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
	public void add(double c, VectorType<Term> v)
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
	public void add(VectorType<Term> v)
	{
		super.add(v);
		setChanged();
		notifyObservers();
	}

	public String toString()
	{
		StringBuilder s = new StringBuilder("{");
		for(Term t : values.keySet())
		{
			s.append(t.toString());
			if (Pref.usePrefBoolean(SHOW_WEIGHTS_PREF, false).value())
			{
				s.append("("); 
				s.append((int)(t.idf() * 100)/100.);
				s.append("), ");
			}
			
		}
		s.append("}");
		return s.toString();
	}
	
	public String termString()
	{
		StringBuilder s = new StringBuilder();
		for (Term t : values.keySet())
		{
			s.append(t.word);
			s.append(" ");
		}
		return s.toString();
	}

	@Override
	public double idfDot(VectorType<Term> v)
	{
		return idfDot(v, false);
	}
	private double idfDot(VectorType<Term> v, boolean noTF)
	{
		HashMap<Term,Double> other = v.map();
		if (other == null || this.norm() == 0 || v.norm() == 0)
			return 0;

		double dot = 0;
		HashMap<Term, Double> vector = this.values;
		synchronized(values) {
			for (Term term : vector.keySet()) {
				if (other.containsKey(term)) 
				{
					double tfIDF = term.idf();
					if (!noTF)
						tfIDF			*= vector.get(term);
					dot += v.get(term) * tfIDF;
				}
			}
		}
		return dot;
	}
	
	public double idfDotNoTF(VectorType<Term> v)
	{
		return idfDot(v, true);
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
	public TermVector unit()
	{
		TermVector v = new TermVector(this);
		v.clamp(1);
		return v;
	}
	
	@Override
	public TermVector simplex ( )
	{
		TermVector v = new TermVector(this);
		for (Term t : v.values.keySet())
		{
			v.values.put(t, 1.0);
		}
		return v;
	}
	
	public void trim(int size)
	{
		TreeMap<Double,Term> highestWeightedTerms = new TreeMap<Double,Term>();
		for (Term t : values.keySet())
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
	
	@Override
	public void set ( Term term, double val )
	{
		super.set(term, val);
		setChanged();
		notifyObservers();
	}
	
}
