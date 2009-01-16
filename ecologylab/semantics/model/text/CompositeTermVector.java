package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.Observer;

import ecologylab.generic.VectorType;

public class CompositeTermVector extends VectorType<Term> implements Observer
{

	public CompositeTermVector()
	{
	}

	private FeatureVector<Term>							compositeTermVector	= new TermVector();

	private Hashtable<VectorType<Term>, Double>	termVectors			= new Hashtable<VectorType<Term>, Double>();

	/**
	 * Adds a Term Vector to this Composite Term Vectors collection, multiplying it by a scalar.
	 * 
	 * @param tv
	 *            The Term Vector you wish to add.
	 * @param multiplier
	 *            The scalar multiple.
	 */
	public void add(double multiplier, VectorType<Term> tv)
	{
		Hashtable<VectorType<Term>, Double> v;
		v = termVectors;
		synchronized (v)
		{
			double oldMultiplier = 0;
			if (v.containsKey(tv))
				oldMultiplier = v.get(tv);
			else
				tv.addObserver(this);
			v.put(tv, oldMultiplier + multiplier);
			compositeTermVector.add(multiplier, tv);
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Adds a Term Vector to this Composite Term Vector's collection
	 * 
	 * @param tv
	 *            The term vector you wish to add.
	 */
	public void add(VectorType<Term> tv)
	{
		if (tv != null)
			add(1, tv);
	}

	/**
	 * Removes a Term Vector from this Composite Term Vectors collection.
	 * 
	 * @param tv
	 *            The Term Vector you wish to remove.
	 */
	public void remove(VectorType<Term> tv)
	{
		Double multiple = termVectors.remove(tv);
		if (multiple != null)
		{
			tv.deleteObserver(this);
			compositeTermVector.add(-multiple, tv);
			setChanged();
			notifyObservers();
		}
	}

	// TODO: look in to sending the old value in "arg",
	// and then subtracting it, then adding the new one
	// instead of rebuilding the whole thing each time.
	public void update(Observable o, Object arg)
	{
		rebuildCompositeTermVector();
		setChanged();
		notifyObservers();
	}

	public void recycle()
	{
		Hashtable<VectorType<Term>, Double> v;
		v = termVectors;
		if (v != null)
			for (VectorType<Term> tv : v.keySet())
				tv.deleteObserver(this);
	}

	private synchronized void rebuildCompositeTermVector()
	{
		Hashtable<VectorType<Term>, Double> v;
		FeatureVector<Term> c = compositeTermVector;
		c = new TermVector(c.size());
		v = termVectors;
		for (VectorType<Term> t : v.keySet())
		{
			c.add(v.get(t), t);
		}
		compositeTermVector = c;
	}

	public double dot(VectorType<Term> v)
	{
		return compositeTermVector.dot(v);
	}

	public Set<Term> elements()
	{
		return compositeTermVector.elements();
	}

	public double get(Term term)
	{
		return compositeTermVector.get(term);
	}

	public HashMap<Term, Double> map()
	{
		return compositeTermVector.map();
	}

	public Set<Double> values()
	{
		return compositeTermVector.values();
	}

	public Set<VectorType<Term>> componentVectors()
	{
		return termVectors.keySet();
	}

	public String toString()
	{
		StringBuilder s = new StringBuilder("[");
		for (VectorType<Term> v : termVectors.keySet())
		{
			s.append(v.toString());
			s.append(", ");
		}
		s.append("]");
		return s.toString();
	}

	public double norm()
	{
		return compositeTermVector.norm();
	}

	@Override
	public double idfDot(VectorType<Term> v)
	{
		return compositeTermVector.idfDot(v);
	}

	@Override
	public VectorType<Term> unit()
	{
		// TODO Auto-generated method stub
		return compositeTermVector.unit();
	}

	@Override
	public int commonDimensions(VectorType<Term> v)
	{
		// TODO Auto-generated method stub
		return compositeTermVector.commonDimensions(v);
	}

	@Override
	public double dotSimplex(VectorType<Term> v)
	{
		// TODO Auto-generated method stub
		return compositeTermVector.dotSimplex(v);
	}
	
	@Override
	public VectorType<Term> simplex()
	{
		return compositeTermVector.simplex();
	}

	@Override
	public double idfDotNoTF(VectorType<Term> v)
	{
		return compositeTermVector.idfDotNoTF(v);
	}

}