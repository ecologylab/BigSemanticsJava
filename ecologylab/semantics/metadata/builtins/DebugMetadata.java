/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.collecting.MetaMetadataRepositoryInit;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataStringBuilder;
import ecologylab.serialization.simpl_inherit;

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
	
	public DebugMetadata(MetadataStringBuilder newTermVector)
	{
		super(MetaMetadataRepositoryInit.DEBUG_META_METADATA);
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
