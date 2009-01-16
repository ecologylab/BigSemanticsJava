package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.Observer;

import ecologylab.generic.IFeatureVector;

public class CompositeTermVector extends Observable implements Observer, ITermVector
{

	public CompositeTermVector()
	{
	}

	private TermVector							compositeTermVector	= new TermVector();

	private Hashtable<ITermVector, Double>	termVectors			= new Hashtable<ITermVector, Double>();

	/**
	 * Adds a Term Vector to this Composite Term Vectors collection, multiplying it by a scalar.
	 * 
	 * @param tv
	 *            The Term Vector you wish to add.
	 * @param multiplier
	 *            The scalar multiple.
	 */
	public void add(double multiplier, ITermVector tv)
	{
		Hashtable<ITermVector, Double> v;
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
	public void add(ITermVector tv)
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
	public void remove(ITermVector tv)
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
		Hashtable<ITermVector, Double> v;
		v = termVectors;
		if (v != null)
			for (ITermVector tv : v.keySet())
				tv.deleteObserver(this);
	}

	private synchronized void rebuildCompositeTermVector()
	{
		Hashtable<ITermVector, Double> v;
		TermVector c = compositeTermVector;
		c = new TermVector(c.size());
		v = termVectors;
		for (IFeatureVector<Term> t : v.keySet())
		{
			c.add(v.get(t), t);
		}
		compositeTermVector = c;
	}

	public double dot(IFeatureVector<Term> v)
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

	public Set<ITermVector> componentVectors()
	{
		return termVectors.keySet();
	}

	public String toString()
	{
		StringBuilder s = new StringBuilder("[");
		for (IFeatureVector<Term> v : termVectors.keySet())
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

	public double idfDot(IFeatureVector<Term> v)
	{
		return compositeTermVector.idfDot(v);
	}

	public TermVector unit()
	{
		// TODO Auto-generated method stub
		return compositeTermVector.unit();
	}

	public int commonDimensions(IFeatureVector<Term> v)
	{
		// TODO Auto-generated method stub
		return compositeTermVector.commonDimensions(v);
	}

	public double dotSimplex(IFeatureVector<Term> v)
	{
		// TODO Auto-generated method stub
		return compositeTermVector.dotSimplex(v);
	}
	
	public TermVector simplex()
	{
		return compositeTermVector.simplex();
	}

	public double idfDotNoTF(IFeatureVector<Term> v)
	{
		return compositeTermVector.idfDotNoTF(v);
	}

}