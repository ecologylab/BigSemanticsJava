/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.xml.ScalarUnmarshallingContext;

/**
 * @author andruid
 *
 */
public class MetadataParsedURLScalarType extends
		MetadataScalarScalarType<MetadataParsedURL, ParsedURL>
{

	public MetadataParsedURLScalarType()
	{
		super(MetadataParsedURL.class, ParsedURL.class);
	}

	@Override
	public MetadataParsedURL getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataParsedURL(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

}
