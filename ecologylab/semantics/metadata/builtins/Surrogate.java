/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.ElementState.simpl_scalar;

/**
 * @author andruid
 *
 */
public class Surrogate extends Metadata
{
	@simpl_composite
	Metadata								clipping;
	
	/**
	 * Text connected to the clipping in the source document.
	 */
	@simpl_scalar
	private MetadataString	context;

	
	@simpl_composite
	Document								source;
	
	@simpl_composite
	Document								outLink;
	
	
	/**
	 * 
	 */
	public Surrogate()
	{
	}

	/**
	 * @param metaMetadata
	 */
	public Surrogate(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
	}

}
