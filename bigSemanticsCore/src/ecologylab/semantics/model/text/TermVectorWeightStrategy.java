package ecologylab.semantics.model.text;

import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

import ecologylab.collections.WeightingStrategy;

public class TermVectorWeightStrategy<E extends TermVectorFeature> extends WeightingStrategy<E>
		implements Observer
{

	private Hashtable<ITermVector, Double>	cachedWeights	= new Hashtable<ITermVector, Double>();

	private ITermVector						referenceVector;

	public TermVectorWeightStrategy ( ITermVector v )
	{
		referenceVector = v;
		v.addObserver(this);
	}
	
	public double getWeight ( ITermVector termVector )
	{
		double weight = -1;
		if (termVector != null)
		{
			Hashtable<ITermVector, Double> cachedWeights = this.cachedWeights;
			synchronized (cachedWeights)
			{
				if (cachedWeights.containsKey(termVector))
					weight = cachedWeights.get(termVector).floatValue();
				else
				{
					weight = termVector.idfDot(referenceVector);
					cachedWeights.put(termVector, weight);
				}
			}
		}
		return weight;
	}

	public double getWeight ( E e )
	{
		return getWeight(e.termVector());
		
	}

	public void insert ( E e )
	{
		if (e != null)
		{
			ITermVector termVector = e.termVector();
			if (termVector != null)
				termVector.addObserver(this);
		}
		super.insert(e);
	}

	public void remove ( E e )
	{
		if (e.termVector() != null)
			e.termVector().deleteObserver(this);
		super.remove(e);
	}

	public void update ( Observable o, Object arg )
	{
		Hashtable<ITermVector, Double> cachedWeights = this.cachedWeights;
		synchronized (cachedWeights)
		{
			if (o == referenceVector)
				cachedWeights.clear();
			else
				cachedWeights.remove(o);
		}
		setChanged();
	}
	
	public ITermVector referenceVector()
	{
		return referenceVector;
	}

}