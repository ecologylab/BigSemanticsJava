package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;

import ecologylab.semantics.model.text.CompositeTermVector;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;


@SuppressWarnings("serial")
public class SemanticInLinks extends ArrayList<SemanticAnchor>
{
	CompositeTermVector semanticInlinkCollection = new CompositeTermVector();

	TermVectorWeightStrategy<SemanticAnchor> weightStrategy;
	
	boolean invalid	= true;
	
	public SemanticInLinks()
	{
	}
	
	public ITermVector termVector()
	{
		return semanticInlinkCollection;
	}
	
	@Override
	public boolean add(SemanticAnchor e)
	{
		return addIfUnique(e);
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
				if(anchorText != null && anchorText.equals(newAnchorText))	// guaranteed to be lower case already
				{
					//This is one case we know we want to ignore this new anchor.
					return false;
				}
			}
			invalid	= true;
			super.add(newAnchor);
			semanticInlinkCollection.add(newAnchor.termVector());
			return true;
		}
		return false;
	}
}
