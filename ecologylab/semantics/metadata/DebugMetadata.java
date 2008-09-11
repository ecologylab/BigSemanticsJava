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
	
	@xml_nested MetadataStringBuilder newTermVector;
	
	/**
	 * 
	 */
	public DebugMetadata()
	{
	}
	
	public DebugMetadata(MetadataStringBuilder termVector, MetadataStringBuilder termWeights, MetadataStringBuilder newTermVector)
	{
		this.termVector 	= termVector;
		this.termWeights 	= termWeights;
		this.newTermVector = newTermVector;
	}
	
	public DebugMetadata(MetadataStringBuilder termVector, MetadataStringBuilder termWeights)
	{
		this.termVector 	= termVector;
		this.termWeights 	= termWeights;
		this.newTermVector = new MetadataStringBuilder();
	}

	/**
	 * @param metaMetadata
	 */
	public DebugMetadata(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
	}

}
