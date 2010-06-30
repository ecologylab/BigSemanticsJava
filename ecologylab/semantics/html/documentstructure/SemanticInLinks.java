package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;

import ecologylab.semantics.model.text.CompositeTermVector;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;


@SuppressWarnings("serial")
public class SemanticInLinks extends ArrayList<SemanticAnchor>
{
	CompositeTermVector semanticInlinkCollection;
	
	public SemanticInLinks()
	{
	}
	
	public ITermVector termVector()
	{
		return semanticInlinkCollection();
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
	
	public CompositeTermVector semanticInlinkCollection()
	{
		CompositeTermVector result = semanticInlinkCollection;
		if (result == null)
		{
			semanticInlinkCollection = new CompositeTermVector();
			result = semanticInlinkCollection;
		}
		
		return result;
	}
	
	public synchronized boolean addIfUnique(SemanticAnchor newAnchor)
	{
		if(!isUnique(newAnchor))
			return false;
		
		return add(newAnchor);
	}

	@Override
	public synchronized boolean add(SemanticAnchor newAnchor)
	{
		semanticInlinkCollection().add(newAnchor.signficance,newAnchor.termVector());
		super.add(newAnchor);
		return true;
	}

	private boolean isUnique(SemanticAnchor newAnchor)
	{
		String newAnchorText = newAnchor.getAnchorText();
		for(SemanticAnchor oldAnchor : this)
		{
			String anchorText = oldAnchor != null ? oldAnchor.getAnchorText() : null;
			if(anchorText != null && anchorText.equals(newAnchorText))	// guaranteed to be lower case already
			{
				//This is one case we know we want to ignore this new anchor.
				return false;
			}
		}
		// Anchor is unique.
		return true;
	}

	/**
	 * Returns a weight for this collection of semantic inlinks, giving a reference ITermVector to weigh against. Usually this is the participant interest TermVector.
	 * @param weightingVector ITermVector to weigh the semantic inlinks with.
	 */
	public double getWeight(ITermVector weightingVector)
	{
		double idfDot = this.termVector().idfDot(weightingVector);
		for(SemanticAnchor anchor : this)
			idfDot += anchor.getSignificance();
		
		return idfDot;
	}
}
