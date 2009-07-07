package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;

import ecologylab.semantics.model.text.TermVectorWeightStrategy;


@SuppressWarnings("serial")
public class SemanticInLinks extends ArrayList<SemanticAnchor>
{

	TermVectorWeightStrategy<SemanticAnchor> weightStrategy;
	
	public SemanticInLinks(TermVectorWeightStrategy<SemanticAnchor> termVectorWeightStrategy)
	{
		weightStrategy = termVectorWeightStrategy;
	}

	public double getWeight()
	{
		double w = 0;
		synchronized (this)
		{
			for(SemanticAnchor anchor : this)
			{
				double weight = weightStrategy.getWeight(anchor);
				w += weight;
			}	
		}
		
		
		return w;
	}
	
	@Override
	public boolean add(SemanticAnchor e)
	{
		return super.add(e);
	}
	
	public synchronized void recycle()
	{
		int index	= size();
		while (--index >= 0)
		{
			SemanticAnchor semanticAnchor		= remove(index);
			semanticAnchor.recycle();
		}
	}
	
	public synchronized boolean addIfUnique(SemanticAnchor newAnchor)
	{
		String newAnchorText = newAnchor.getAnchorText();
		if(newAnchorText != null)
		{
			for(SemanticAnchor oldAnchor : this)
			{
				String anchorText = oldAnchor != null ? oldAnchor.getAnchorText() : null;
				if(anchorText != null && anchorText.equalsIgnoreCase(newAnchorText))
				{
					//This is one case we know we want to ignore this new anchor.
					return false;
				}
			}
			return add(newAnchor);
		}
		return false;
	}
}
