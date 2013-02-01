/**
 * 
 */
package ecologylab.bigsemantics.metadata.scalar.types;

import ecologylab.bigsemantics.metadata.scalar.MetadataInteger;
import ecologylab.serialization.ScalarUnmarshallingContext;

/**
 * @author andruid
 *
 */
public class MetadataIntegerScalarType extends MetadataScalarType<MetadataInteger, Integer>
{

	public MetadataIntegerScalarType()
	{
		super(MetadataInteger.class, Integer.class, null, null);
	}

	@Override
	public MetadataInteger getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataInteger(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

}
