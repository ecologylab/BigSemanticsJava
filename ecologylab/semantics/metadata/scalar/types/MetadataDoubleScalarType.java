/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.semantics.metadata.scalar.MetadataDouble;
import ecologylab.semantics.metadata.scalar.MetadataFloat;
import ecologylab.serialization.ScalarUnmarshallingContext;

/**
 * @author andruid
 *
 */
public class MetadataDoubleScalarType extends MetadataScalarType<MetadataDouble, Double>
{

	public MetadataDoubleScalarType()
	{
		super(MetadataDouble.class, Double.class, null, null);
	}

	@Override
	public MetadataDouble getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataDouble(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

}
