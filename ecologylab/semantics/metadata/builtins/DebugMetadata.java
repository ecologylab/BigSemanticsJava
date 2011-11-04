/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.mm_name;
import ecologylab.semantics.metadata.scalar.MetadataStringBuilder;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Dynamically generated fields, only for debugging purposes by developers.
 * Not for normal users.
 * 
 * @author andruid
 */
@simpl_inherit
public class DebugMetadata extends Metadata
{
	@simpl_scalar 
	@mm_name("new_term_vector")
	MetadataStringBuilder newTermVector;
	
	/**
	 * 
	 */
	public DebugMetadata()
	{
	}
	
	public DebugMetadata(MetaMetadata debugMetaMetadata, MetadataStringBuilder newTermVector)
	{
		super(debugMetaMetadata);
		this.newTermVector = newTermVector;
	}

	public MetadataStringBuilder getNewTermVectorMetadata()
	{
		return newTermVector;
	}

	public void setNewTermVectorMetadata(MetadataStringBuilder newTermVector)
	{
		this.newTermVector = newTermVector;
	}
}
