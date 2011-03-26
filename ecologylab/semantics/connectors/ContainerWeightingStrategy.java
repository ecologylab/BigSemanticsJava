/**
 * 
 */
package ecologylab.semantics.connectors;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVectorWeightStrategy;


public class ContainerWeightingStrategy extends TermVectorWeightStrategy<DocumentClosure>
{

	private static final double	TOO_MANY_TIMEOUTS_WEIGHT	= -1;

	public ContainerWeightingStrategy ( ITermVector v )
	{
		super(v);
	}

	@Override
	public double getWeight ( DocumentClosure e )
	{
		SemanticsSite site = e.getSite();
		
		SemanticInLinks semanticInlinks = e.getSemanticInlinks();
		double inLinkWeight = semanticInlinks == null ? 0 : semanticInlinks.getWeight(this.referenceVector());
		
		return getWeightWithSite(e,site) * (inLinkWeight + 1);
	}
	
	protected double getWeightWithSite( DocumentClosure e, SemanticsSite site)
	{
		if ((site != null)&& (site.tooManyTimeouts()))
			return TOO_MANY_TIMEOUTS_WEIGHT;
		
		double termVectorWeight = super.getWeight(e);

		if (site != null)
		{
			termVectorWeight = (termVectorWeight + 1) * site.weightingFactor();
		}
		return termVectorWeight; 
	}

}
