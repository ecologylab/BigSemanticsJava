package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;

import ecologylab.collections.ConcurrentHashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.model.text.CompositeTermVector;
import ecologylab.semantics.model.text.ITermVector;


@SuppressWarnings("serial")
public class SemanticInLinks extends ConcurrentHashMapArrayList<ParsedURL, SemanticAnchor>
{
	CompositeTermVector semanticInlinkCollection;
	
	public SemanticInLinks()
	{
	}
	
	public synchronized void recycle()
	{
		int index	= size();
		while (--index >= 0)
		{
			SemanticAnchor semanticAnchor		= remove(index);
			semanticAnchor.recycle();
		}
		clear();
		
		if (semanticInlinkCollection != null)
			semanticInlinkCollection.recycle();
		
		semanticInlinkCollection = null;
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
	
	public synchronized boolean add(SemanticAnchor newAnchor)
	{
		SemanticAnchor oldAnchor	= putIfAbsent(newAnchor.sourcePurl(), newAnchor);
		if (oldAnchor == null)	//TODO -- should we count and incorporate new terms?!
			semanticInlinkCollection().add(newAnchor.significance, newAnchor.termVector());
		return true;
	}

	/**
	 * Returns a weight for this collection of semantic inlinks, giving a reference ITermVector to weigh against. Usually this is the participant interest TermVector.
	 * @param weightingVector ITermVector to weigh the semantic inlinks with.
	 */
	public double getWeight(ITermVector weightingVector)
	{
		double idfDot = this.semanticInlinkCollection().idfDot(weightingVector);
		for(SemanticAnchor anchor : this)
		{
			idfDot += anchor.getSignificance();
		}	
		return idfDot;
	}

	/**
	 * Returns 1 if no links exist, else the mean of the significance's of its contents
	 * @return
	 */
	public float meanSignificance()
	{
		if (this.size() == 0)
			return 1;
		float meanSig = 0;		
		for(SemanticAnchor a : this)
			meanSig += a.getSignificance(); 
		meanSig /= this.size();
		return meanSig;
	}
}
