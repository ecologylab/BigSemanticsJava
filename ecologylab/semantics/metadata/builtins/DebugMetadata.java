/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataStringBuilder;
import ecologylab.semantics.metametadata.MetaMetadata;

/**
 * Dynamically generated fields, only for debugging purposes by developers.
 * Not for normal users.
 * 
 * @author andruid
 */
public class DebugMetadata extends Metadata
{
	@xml_leaf MetadataStringBuilder newTermVector;
	
	/**
	 * 
	 */
	public DebugMetadata()
	{
	}
	
	public DebugMetadata(MetadataStringBuilder newTermVector)
	{

		this.newTermVector = newTermVector;
	}

	/**
	 * @param metaMetadata
	 */
	public DebugMetadata(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
	}

}
