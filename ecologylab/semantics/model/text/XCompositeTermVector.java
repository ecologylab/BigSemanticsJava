package ecologylab.semantics.model.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.Observer;

public class XCompositeTermVector extends Observable implements Observer,
		ITermVector
{

	public XCompositeTermVector()
	{
	}

	private XTermVector compositeTermVector;
	private HashMap<ITermVector, Double> termVectors;

	/**
	 * Adds a Term Vector to this Composite Term Vectors collection, multiplying
	 * it by a scalar.
	 * 
	 * @param tv
	 *          The Term Vector you wish to add.
	 * @param multiplier
	 *          The scalar multiple.
	 */
	public void add(double multiplier, ITermVector tv)
	{
		HashMap<ITermVector, Double> v;
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
	public void add(ITermVector tv)
	{
		add(1, tv);
	}

	/**
	 * Removes a Term Vector from this Composite Term Vectors collection.
	 * 
	 * @param tv
	 *          The Term Vector you wish to remove.
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
		if ((String) arg == "delete")
		{
			ITermVector tv = (ITermVector) o;
			remove(tv);
		}
		rebuildCompositeTermVector();
		setChanged();
		notifyObservers();
	}

	public void recycle()
	{
		HashMap<ITermVector, Double> v;
		v = termVectors;
		if (v != null)
			for (ITermVector tv : v.keySet())
				tv.deleteObserver(this);
	}

	private synchronized void rebuildCompositeTermVector()
	{
		HashMap<ITermVector, Double> v;
		XTermVector c = compositeTermVector;
		c = new XTermVector(c.size());
		v = termVectors;
		for (ITermVector t : v.keySet())
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

}