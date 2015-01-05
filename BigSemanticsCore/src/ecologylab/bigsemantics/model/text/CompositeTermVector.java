package ecologylab.bigsemantics.model.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import ecologylab.generic.IFeatureVector;
/**
 * A CompositeTermVector acts like a TermVector on the outside, but on the inside
 * it maintains a reference to each TermVector added to it.  This means that if the content
 * of a TermVector is modified, this modification bubbles up through the CompositeTermVector
 * which is observing all it's component term vectors.
 * @author jmole
 *
 */
public class CompositeTermVector extends Observable implements Observer, ITermVector
{
	private TermVector						compositeTermVector	= new TermVector();

	private HashMap<ITermVector, Double>	termVectors	= new HashMap<ITermVector, Double>();

	public CompositeTermVector ()
	{
	}

	/**
	 * Adds a Term Vector to this Composite Term Vectors collection, multiplying it by a scalar.
	 * 
	 * @param tv
	 *            The Term Vector you wish to add.
	 * @param multiplier
	 *            The scalar multiple.
	 */
	@Override
	public void add ( double multiplier, ITermVector tv )
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
	 *            The term vector you wish to add.
	 */
	@Override
	public void add ( ITermVector tv )
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
	public void remove ( ITermVector tv )
	{
		//FIXME this is a bandaid for a race condition elsewhere, in which still in use term vectors
		// are getting recycled. where is this happening? why?
		if (termVectors == null)
			return;
		
		Double removal = null;
		synchronized (termVectors)
		{
			removal = termVectors.remove(tv);
		}
		if (removal != null)
		{
			tv.deleteObserver(this);
			compositeTermVector.add(-removal, tv);
			setChanged();
			notifyObservers();
		}
	}
	
	private boolean notifiedAlready;
	@Override
	public void update ( Observable o, Object arg )
	{
		Set set = (Set) arg;
		if (set == null || set.contains(this))
			return;
		set.add(this);
		
		notifiedAlready = false;
		rebuildCompositeTermVector();
		setChanged();
		if(notifiedAlready == false)
		{
		   notifiedAlready = true;
		   notifyObservers(set);
		}
	}
	
	@Override
	public void notifyObservers()
	{
		notifyObservers(new HashSet());
	}

	@Override
	public synchronized void recycle ( )
	{
		if (!hasObservers())
		{
			if (termVectors != null)
			{
				for (ITermVector tv : termVectors.keySet())
				{
					tv.deleteObserver(this);
					if (!tv.hasObservers())
						tv.recycle();
				}
				termVectors.clear();
				termVectors				= null;
			}
			if (compositeTermVector != null)
			{
				compositeTermVector.recycle();
				compositeTermVector	= null;
			}
		}
	}
	
	@Override
	public boolean isRecycled()
	{
		return compositeTermVector == null;
	}

	private void rebuildCompositeTermVector ( )
	{
		TermVector c = new TermVector(compositeTermVector.size());
		synchronized (termVectors)
		{
			for (IFeatureVector<Term> t : termVectors.keySet())
				c.add(termVectors.get(t), t);
		}
		compositeTermVector = c;
	}

	public void reinitialize()
	{
		compositeTermVector	= new TermVector();
		termVectors			= new HashMap<ITermVector, Double>();
	}

	@Override
	public double dot ( IFeatureVector<Term> v )
	{
		return compositeTermVector.dot(v);
	}

	@Override
	public Set<Term> elements ( )
	{
		return compositeTermVector.elements();
	}

	@Override
	public double get ( Term term )
	{
		return compositeTermVector.get(term);
	}

	@Override
	public Map<Term, Double> map ( )
	{
		return compositeTermVector != null ? compositeTermVector.map() : null;
	}

	@Override
	public Set<Double> values ( )
	{
		return compositeTermVector.values();
	}

	public Set<ITermVector> componentVectors ( )
	{
		return termVectors.keySet();
	}

	@Override
	public String toString ( )
	{
		if (termVectors == null)
			return "";
			
		StringBuilder s = new StringBuilder("[");
		synchronized (termVectors)
		{
			for (IFeatureVector<Term> v : termVectors.keySet())
			{
				s.append(v.toString());
				s.append(", ");
			}
		}
		s.append("]");
		return s.toString();
	}

	@Override
	public double norm ( )
	{
		return compositeTermVector.norm();
	}

	@Override
	public double max ( )
	{
		return compositeTermVector.max();
	}

	@Override
	public double idfDot ( IFeatureVector<Term> v )
	{
		return (compositeTermVector != null) ? compositeTermVector.idfDot(v) : Double.MIN_VALUE;
	}

	@Override
	public TermVector unit ( )
	{
		return compositeTermVector.unit();
	}

	@Override
	public int commonDimensions ( IFeatureVector<Term> v )
	{
		return compositeTermVector.commonDimensions(v);
	}

	@Override
	public double dotSimplex ( IFeatureVector<Term> v )
	{
		return compositeTermVector.dotSimplex(v);
	}

	@Override
	public TermVector simplex ( )
	{
		return compositeTermVector.simplex();
	}

	@Override
	public double idfDotSimplex ( IFeatureVector<Term> v )
	{
		return compositeTermVector.idfDotSimplex(v);
	}
	
	public int size()
	{
		return compositeTermVector.size();
	}

	@Override
	public boolean hasObservers()
	{
		return countObservers() > 0;
	}


}