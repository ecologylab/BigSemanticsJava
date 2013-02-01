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
	}


	@Override
	public double getWeight(SemanticAnchor anchor)
	{
		return super.getWeight(anchor) * anchor.getSignificance();
	}

}
