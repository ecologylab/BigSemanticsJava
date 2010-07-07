/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.connectors.InfoCollectorBase;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataStringBuilder;
import ecologylab.serialization.Hint;
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
	@simpl_scalar @simpl_hints(Hint.XML_LEAF) MetadataStringBuilder newTermVector;
	
	/**
	 * 
	 */
	public DebugMetadata()
	{
	}
	
	public DebugMetadata(MetadataStringBuilder newTermVector)
	{
		super(InfoCollectorBase.getMM(DebugMetadata.class));
		this.newTermVector = newTermVector;
	}
}
