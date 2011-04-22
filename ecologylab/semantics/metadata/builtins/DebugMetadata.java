/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.collecting.NewInfoCollector;
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
		super(NewInfoCollector.getMM(DebugMetadata.class));
		this.newTermVector = newTermVector;
	}
}
