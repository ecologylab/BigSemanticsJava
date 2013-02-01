/**
 * 
 */
package ecologylab.bigsemantics.metadata.scalar.types;

import java.util.Date;

import ecologylab.bigsemantics.metadata.scalar.MetadataDate;
import ecologylab.serialization.ScalarUnmarshallingContext;

/**
 * @author andruid
 *
 */
public class MetadataDateScalarType extends MetadataScalarType<MetadataDate, Date>
{
	public MetadataDateScalarType()
	{
		super(MetadataDate.class, Date.class, null, null);
	}


	@Override
	public MetadataDate getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataDate(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

}
