/**
 * 
 */
package ecologylab.semantics.connectors;

import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.model.text.ITermVector;

public class DownloadContainerWeightingStrategy extends ContainerWeightingStrategy
{
	
	private static final double	RECYCLED_WEIGHT	= - Double.MAX_VALUE / 2;
	private static final double	ALREADY_DOWNLOADING_WEIGHT	= -0.5;

	public DownloadContainerWeightingStrategy ( ITermVector v )
	{
		super(v);
	}
	
	@Override
	public double getWeight (DocumentClosure e )
	{
		SemanticsSite site				= e.getSite();
		Document metadata	= e.getDocument();
		if (metadata == null)
		{
			error("null metadata! yikes!!! " + e.initialPURL);
			return RECYCLED_WEIGHT;
		}
		boolean isDefaultMetadata	= Document.class == metadata.getClass();
		double weight = (site == null || (isDefaultMetadata && site.hasQueuedDownloadables()))
										? ALREADY_DOWNLOADING_WEIGHT 
										: super.getWeight(e);
		
		e.cachedWeight = weight;
		return weight;
	}

}