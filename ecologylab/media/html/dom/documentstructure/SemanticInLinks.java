package ecologylab.media.html.dom.documentstructure;

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
		for(SemanticAnchor anchor : this)
		{
			double weight = weightStrategy.getWeight(anchor);
			weight /= anchor.withinSite ? 2 : 1;
			w += weight;
		}
		
		return w;
	}
	
	@Override
	public boolean add(SemanticAnchor e)
	{
		return super.add(e);
	}
}
