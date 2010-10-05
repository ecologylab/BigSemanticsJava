package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;

import com.sun.org.apache.bcel.internal.generic.NEWARRAY;

import ecologylab.net.ParsedURL;
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
	
	@Override
	public synchronized boolean add(SemanticAnchor newAnchor)
	{
		semanticInlinkCollection().add(newAnchor.significance, newAnchor.termVector());
		super.add(newAnchor);
		return true;
	}

	/**
	 * Returns a weight for this collection of semantic inlinks, giving a reference ITermVector to weigh against. Usually this is the participant interest TermVector.
	 * @param weightingVector ITermVector to weigh the semantic inlinks with.
	 */
	public double getWeight(ITermVector weightingVector)
	{
		double idfDot = this.semanticInlinkCollection().idfDot(weightingVector);
		ArrayList<ParsedURL> sourcePurls = new ArrayList<ParsedURL>();
		for(SemanticAnchor anchor : this)
		{
			//Only boost weight from unique purls.
			//This is possibly causing us to crawl through a sitemap,
			// in the cases where the links are found across pages.
			if(!sourcePurls.contains(anchor.sourcePurl))
				idfDot += anchor.getSignificance();
			sourcePurls.add(anchor.sourcePurl);
		}
		
		return idfDot;
	}

	public boolean containsPurl(ParsedURL purl)
	{
		for(SemanticAnchor anchor : this)
			if(anchor.sourcePurl == purl)
				return true;
		return false;
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
