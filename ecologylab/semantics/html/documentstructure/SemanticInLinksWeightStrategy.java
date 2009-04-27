/**
 * 
 */
package ecologylab.semantics.html.documentstructure;

import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;

/**
 * Weighting strategy for semanticInLinks of a container.
 * Penalizes contribution of links from within the same site.
 * @author sashikanth
 */
public class SemanticInLinksWeightStrategy extends TermVectorWeightStrategy<SemanticAnchor>
{

	public SemanticInLinksWeightStrategy(ITermVector v)
	{
		super(v);
		// TODO Auto-generated constructor stub
	}


	@Override
	public double getWeight(SemanticAnchor anchor)
	{
		double siteFactor = anchor.withinSite ? 2 : 1;
		return super.getWeight(anchor) / siteFactor;
	}

}
