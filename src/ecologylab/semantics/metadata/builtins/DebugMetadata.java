/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.mm_name;
import ecologylab.semantics.metadata.scalar.MetadataStringBuilder;
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
	
	public DebugMetadata(MetadataStringBuilder newTermVector)
	{
		super(SemanticsSessionScope.get().DEBUG_META_METADATA);
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
