/**
 * 
 */
package ecologylab.semantics.metadata;

import ecologylab.semantics.library.scalar.MetadataStringBuilder;
import ecologylab.semantics.metametadata.MetaMetadata;

/**
 * Dynamically generated fields, only for debugging purposes by developers.
 * Not for normal users.
 * 
 * @author andruid
 */
public class DebugMetadata extends Metadata
{
	@xml_nested MetadataStringBuilder	termVector;
	
	@xml_nested MetadataStringBuilder	termWeights;
	
	/**
	 * 
	 */
	public DebugMetadata()
	{
	}
	
	public DebugMetadata(MetadataStringBuilder termVector, MetadataStringBuilder termWeights)
	{
		this.termVector 	= termVector;
		this.termWeights 	= termWeights;
	}

	/**
	 * @param metaMetadata
	 */
	public DebugMetadata(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
	}

}
