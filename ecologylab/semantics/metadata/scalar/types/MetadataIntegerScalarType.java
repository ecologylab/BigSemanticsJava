/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.semantics.metadata.scalar.MetadataInteger;
import ecologylab.xml.ScalarUnmarshallingContext;

/**
 * @author andruid
 *
 */
public class MetadataIntegerScalarType extends MetadataScalarScalarType<MetadataInteger, Integer>
{

	public MetadataIntegerScalarType()
	{
		super(MetadataInteger.class, Integer.class);
	}

	@Override
	public MetadataInteger getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataInteger(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

}
