package ecologylab.bigsemantics.html.documentstructure;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.model.text.CompositeTermVector;
import ecologylab.bigsemantics.model.text.ITermVector;
import ecologylab.net.ParsedURL;

//TODO -- make serializable!

@SuppressWarnings("serial")
public class SemanticInLinks extends ConcurrentHashMap<ParsedURL, SemanticAnchor> implements Iterable<SemanticAnchor>
{
	private CompositeTermVector compositeTermVector;
	
	private Document 	ancestor;
	
	private int				generation;
	
	private boolean		fromSemanticAction;
	
	public SemanticInLinks()
	{
	}
	
	public synchronized void recycle()
	{
		int index	= size();
		while (--index >= 0)
		{
			SemanticAnchor semanticAnchor		= remove(index);
			if (semanticAnchor != null)
				semanticAnchor.recycle();
		}
		clear();
		
		if (compositeTermVector != null)
		{
			compositeTermVector.recycle();
			compositeTermVector	= null;
		}
		ancestor									= null;
	}
	
	public CompositeTermVector semanticInlinkCollection()
	{
		CompositeTermVector result = compositeTermVector;
		if (result == null)
		{
			compositeTermVector = new CompositeTermVector();
			result = compositeTermVector;
		}
		
		return result;
	}
	
	public synchronized boolean add(SemanticAnchor newAnchor, Document source)
	{
		SemanticAnchor oldAnchor	= putIfAbsent(newAnchor.sourcePurl(), newAnchor);
		if (oldAnchor == null)	
		{
			semanticInlinkCollection().add(newAnchor.getSignificance(), newAnchor.termVector());
			if (newAnchor.fromSemanticAction())
				fromSemanticAction	= true;
		}
		else
		{
			//TODO -- should we count and incorporate new terms?!
		}
		add(source);
		return true;
	}

	/**
	 * @param source
	 */
	public void add(Document source)
	{
		if (source != null)
		{
			int sourceBasedGeneration	= source.getGeneration() + 1;
			if (sourceBasedGeneration < generation || ancestor == null)
			{
				generation	= sourceBasedGeneration;
				ancestor		= source;
			}
		}
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

	@Override
	public Iterator<SemanticAnchor> iterator() 
	{
		return values().iterator();
	}

	/**
	 * @return the ancestor
	 */
	public Document getAncestor()
	{
		return ancestor;
	}

	/**
	 * @return the generation
	 */
	public int getGeneration()
	{
		return generation;
	}
	
	public int getEffectiveGeneration()
	{
		return fromSemanticAction ? (ancestor != null ? ancestor.getGeneration() : 0) : generation;
	}
	
	public void merge(SemanticInLinks oldInlinks)
	{
		for (SemanticAnchor inlink : oldInlinks)
		{
			add(inlink, null);
		}
	}
}
