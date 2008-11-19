package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.Observer;

import ecologylab.generic.VectorType;
import ecologylab.model.text.TermVector;

public class XCompositeTermVector extends VectorType<XTerm> implements Observer
{

	public XCompositeTermVector() {}

	private XTermVector compositeTermVector = new XTermVector(); 
	private Hashtable<VectorType<XTerm>, Double> termVectors = new Hashtable<VectorType<XTerm>, Double>();

	/**
	 * Adds a Term Vector to this Composite Term Vectors collection, multiplying
	 * it by a scalar.
	 * 
	 * @param tv
	 *          The Term Vector you wish to add.
	 * @param multiplier
	 *          The scalar multiple.
	 */
	public void add(double multiplier, VectorType<XTerm> tv)
	{
		Hashtable<VectorType<XTerm>, Double> v;
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
	 *          The term vector you wish to add.
	 */
	public void add(VectorType<XTerm> tv)
	{
		if(tv != null)
			add(1, tv);
	}

	/**
	 * Removes a Term Vector from this Composite Term Vectors collection.
	 * 
	 * @param tv
	 *          The Term Vector you wish to remove.
	 */
	public void remove(VectorType<XTerm> tv)
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
		Hashtable<VectorType<XTerm>, Double> v;
		v = termVectors;
		if (v != null)
			for (VectorType<XTerm> tv : v.keySet())
				tv.deleteObserver(this);
	}

	private synchronized void rebuildCompositeTermVector()
	{
		Hashtable<VectorType<XTerm>, Double> v;
		XTermVector c = compositeTermVector;
		c = new XTermVector(c.size());
		v = termVectors;
		for (VectorType<XTerm> t : v.keySet())
		{
			c.add(v.get(t), t);
		}
		compositeTermVector = c;
	}

	public double dot(VectorType<XTerm> v)
	{	  
		return compositeTermVector.dot(v);
	}

	public Set<XTerm> elements()
	{
		return compositeTermVector.elements();
	}

	public double get(XTerm term)
	{
		return compositeTermVector.get(term);
	}

	public HashMap<XTerm, Double> map()
	{
		return compositeTermVector.map();
	}

	public Set<Double> values()
	{
		return compositeTermVector.values();
	}
	
	public Set<VectorType<XTerm>> componentVectors()
	{
	  return termVectors.keySet();
	}
	
	public String toString()
	{
	  StringBuilder s = new StringBuilder("[");
	  for(VectorType<XTerm> v : termVectors.keySet())
	    s.append(v.toString() + "(" + termVectors.get(v) + "), ");
	  s.append("]");
	  return s.toString();
	}
	
	public double norm() 
	{
	  return compositeTermVector.norm();
	}

  @Override
  public double idfDot(VectorType<XTerm> v)
  {
    return compositeTermVector.idfDot(v);
  }

}