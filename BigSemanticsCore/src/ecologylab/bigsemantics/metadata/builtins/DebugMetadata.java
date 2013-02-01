/**
 * 
 */
package ecologylab.bigsemantics.metadata.builtins;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.metadata.builtins.declarations.DebugMetadataDeclaration;
import ecologylab.bigsemantics.metadata.scalar.MetadataStringBuilder;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * Dynamically generated fields, only for debugging purposes by developers.
 * Not for normal users.
 * 
 * @author andruid
 */
@simpl_inherit
public class DebugMetadata extends DebugMetadataDeclaration
{
	
//	@simpl_scalar 
//	@mm_name("new_term_vector")
//	MetadataStringBuilder newTermVector;
	
	public DebugMetadata()
	{
		super();
	}
	
	public DebugMetadata(MetaMetadataCompositeField mmd)
	{
		super(mmd);
	}
	
	public DebugMetadata(MetadataStringBuilder newTermVector, SemanticsGlobalScope scope)
	{
		super(scope.DEBUG_META_METADATA);
		this.setNewTermVectorMetadata(newTermVector);
	}

}
