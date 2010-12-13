/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.SerializationContext;

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

	@Override
	public String getCSharptType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDbType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getObjectiveCType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String marshall(MetadataParsedURL instance, SerializationContext serializationContext)
	{
		return operativeScalarType().marshall(instance.getValue(), serializationContext);
	}

	@Override
	public boolean affordsInterestExpression()
	{
		return false;
	}
}
