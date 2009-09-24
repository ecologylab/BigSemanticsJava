/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_nested;

/**
 * This class represents a linked nested object in a metadata reference graph.
 * The object will be referred to later.
 * 
 * @author andruid
 */
@xml_inherit
public class MetadataReference extends Metadata
{
	@xml_nested MetadataString		summary;
	@xml_nested MetadataParsedURL	location;
	
	Metadata											entity;
	
	
	/**
	 * 
	 */
	public MetadataReference()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param metaMetadata
	 */
	public MetadataReference(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
		// TODO Auto-generated constructor stub
	}

}
