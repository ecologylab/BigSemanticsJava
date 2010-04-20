package ecologylab.semantics.model.text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.generic.FeatureVector;
import ecologylab.generic.IFeatureVector;
import ecologylab.generic.StringTools;
import ecologylab.semantics.html.utils.StringBuilderUtils;

/**
 * TermVector represents a collection of Terms, each associated with a particular value. Usually
 * this value represents the Term's frequency in a particular document, however, it may also
 * represent the amount of interest in a Term, as is the case in InterestModel.java
 * 
 * @author jmole
 * 
 */
public class TermVector extends FeatureVector<Term> implements ITermVector
{

	private static final Double	DEFAULT_WEIGHT	= 1.0;

	private static final String	SHOW_WEIGHTS_PREF	= "show_weights";

	public static Pattern				WORD_REGEX			= Pattern.compile("[a-zA-Z]+(-[a-zA-Z]+)*([a-zA-Z]+)");

	public TermVector ()
	{
	}

	public TermVector ( IFeatureVector<Term> tv )
	{
		super(tv);
	}

	public TermVector ( int size )
	{
		super(size); // lol
	}

	/**
	 * Creates a new TermVector from a given String, using the TermDictionary to stem and find the
	 * Term associated with each word.
	 * 
	 * @param input
	 */
	public TermVector ( CharSequence input )
	{
		reset(input);
	}

	/**
	 * Totally reconstructs this term vector based on a new string. Useful for maintaining the
	 * observers and such while changing the actual terms.
	 * 
	 * @param input
	 */
	
	public void reset (CharSequence input)
	{
		super.reset();
//		StringTools.toLowerCase(s);
		add(input);
	}

	public void add(CharSequence input)
	{
		add(input, DEFAULT_WEIGHT);
	}	
	/**
	 * @param input
	 */
	public void add(CharSequence input, Double weight)
	{
		Matcher m = WORD_REGEX.matcher(input);
		StringBuilder termBuffy	= StringBuilderUtils.acquire();
		while (m.find())
		{
			int start = m.start();
			termBuffy.append(input, start, m.end());
			StringTools.toLowerCase(termBuffy);
			addWithoutNotify(TermDictionary.getTermForWord(termBuffy), weight);
			StringTools.clear(termBuffy);
		}
		StringBuilderUtils.release(termBuffy);
		setChanged();
		notifyObservers();
	}

	private void addWithoutNotify ( Term term, Double val )
	{
		if (term == null || term.isStopword())
			return;
		super.add(term, val);
	}

	public void add ( Term term, Double val )
	{
		if (!term.isStopword())
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
	public void multiply ( IFeatureVector<Term> v )
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
	public void multiply ( double c )
	{
		super.multiply(c);
		setChanged();
		notifyObservers();
	}

	/**
	 * Pairwise addition of this vector by some other vector times some constant.<br>
	 * i.e. this + (c*v)<br>
	 * Vector v is not modified.
	 * 
	 * @param c
	 *            Constant which Vector v is multiplied by.
	 * @param v
	 *            Vector to add to this one
	 */
	public void add ( double c, ITermVector v )
	{
		add(c, (IFeatureVector<Term>) v);
	}
	
	/**
	 * Adds another Vector to this Vector, in-place.
	 * 
	 * @param v
	 *            Vector to add to this
	 */
	public void add (ITermVector v )
	{
		add((IFeatureVector<Term>) v);
	}

	/**
	 * Pairwise addition of this vector by some other vector times some constant.<br>
	 * i.e. this + (c*v)<br>
	 * Vector v is not modified.
	 * 
	 * @param c
	 *            Constant which Vector v is multiplied by.
	 * @param v
	 *            Vector to add to this one
	 */
	public void add ( double c, IFeatureVector<Term> v )
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
	public void add ( IFeatureVector<Term> v )
	{
		super.add(v);
		setChanged();
		notifyObservers();
	}

	public String toString ( )
	{
		if (values == null)
			return "{}";
		StringBuilder s = new StringBuilder("{");
		synchronized (values)
		{
			for (Term t : values.keySet())
			{
				s.append(t.toString());
				if (Pref.usePrefBoolean(SHOW_WEIGHTS_PREF, false).value())
				{
					s.append("(");
					s.append((int) (t.idf() * 100) / 100.);
					s.append("),");
				}
				s.append(" ");
			}
		}
		s.append("}");
		return s.toString();
	}

	public String termString ( )
	{
		if (values == null)
			return "";
		StringBuilder s = new StringBuilder();
		synchronized (values)
		{
			for (Term t : values.keySet())
			{
				s.append(t.getWord());
				s.append(" ");
			}
		}
		return s.toString();
	}

	public double idfDot ( IFeatureVector<Term> v )
	{
		return idfDot(v, false);
	}

	public double idfDotSimplex ( IFeatureVector<Term> v )
	{
		return idfDot(v, true);
	}

	private double idfDot ( IFeatureVector<Term> v, boolean simplex )
	{
		Map<Term, Double> other = v.map();
		if (other == null || other.size() == 0 || this.values == null || this.norm() == 0 || v.norm() == 0)
			return 0;

		double dot = 0;
		HashMap<Term, Double> vector = this.values;
		synchronized (values)
		{
			for (Term term : vector.keySet())
			{
				if (other.containsKey(term))
				{
					double tfIDF = term.idf() * vector.get(term);
					if (!simplex)
						tfIDF *= other.get(term);
					dot += tfIDF;
				}
			}
			dot /= values.size();
		}
		return dot;
	}

	public void clamp ( double clampTo )
	{
		super.clamp(clampTo);
		setChanged();
		notifyObservers();
	}

	public void clampExp ( double clampTo )
	{
		super.clampExp(clampTo);
		setChanged();
		notifyObservers();
	}

	@Override
	public TermVector unit ( )
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

	/**
	 * IDF trim (ignores tf).
	 * Deletes lowest weighted terms until the TermVector only has "size" terms. If "size" is
	 * greater than the number of Terms contained in this TermVector, this method does nothing.
	 * 
	 * @param size
	 *            the new size (i.e. number of Terms) of the TermVector.
	 */
	public void trim ( int size )
	{
		if (values == null)
			return;
		
		if (size >= values.size())
			return;

		synchronized (values)
		{			
			TreeMap<Term, Double> sortedTerms = new TreeMap<Term, Double>(values);	// idf sorted, because Term implements Comparable<Term>
			values.clear();
			for (Term t : sortedTerms.keySet())
			{
				if (values.size() == size)
					break;
				
				values.put(t, sortedTerms.get(t));
			}
		}
	}
	
	public Term[] tfIdfTrim(int size)
	{
		synchronized (values)
		{			
			TreeMap<Double, Term> tfIdfMap = buildTfIdfMap();
			
			Term[] result	= new Term[size];
			int i					= 0;
			for (Term term : tfIdfMap.values())
			{
				result[i++]	= term;
				if (i >= size)
					break;
			}
			return result;
		}
	}

	/**
	 * @return
	 */
	private TreeMap<Double, Term> buildTfIdfMap()
	{
		// build map ordered by tf-idf
		TreeMap<Double, Term> tfIdfMap	= new TreeMap<Double, Term>(reverse);
		for (Term term : values.keySet())
		{
			double tf		= values.get(term);
			double idf	= term.idf();
			tfIdfMap.put(tf*idf, term);
		}
		return tfIdfMap;
	}	
	public ArrayList<Term> tfIdfTrim(double threshold)
	{
		synchronized (values)
		{			
			TreeMap<Double, Term> tfIdfMap = buildTfIdfMap();
			
			ArrayList<Term> result	= new ArrayList<Term>();
			for (Double tfIdf : tfIdfMap.keySet())
			{
				if (tfIdf < threshold)
					break;
				Term term	= tfIdfMap.get(tfIdf);
				result.add(term);
			}
			return result;
		}
	}
	
	public double tfIdfMean()
	{
		double result	= 0;
		Set<Term> keySet = values.keySet();
		int n					= keySet.size();
		for (Term term : keySet)
		{
			result	+= values.get(term) * term.idf();
		}
		return result / n;
	}
	
	Comparator<Double> reverse	= new Comparator<Double>()
	{

		@Override
		public int compare(Double d1, Double d2)
		{
			if (d2 > d1)
				return 1;
			else if (d2 == d1)
				return 0;
			else
				return -1;
		}
		
	};

	@Override
	public void set ( Term term, Double val )
	{
		super.set(term, val);
		setChanged();
		notifyObservers();
	}
	

	public boolean hasObservers()
	{
		return countObservers() > 0;
	}


}
