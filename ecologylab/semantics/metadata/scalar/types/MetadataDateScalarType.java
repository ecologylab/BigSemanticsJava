/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import java.util.Date;

import ecologylab.semantics.metadata.scalar.MetadataDate;
import ecologylab.serialization.ScalarUnmarshallingContext;

/**
 * @author andruid
 *
 */
public class MetadataDateScalarType extends MetadataScalarScalarType<MetadataDate, Date>
{
	public MetadataDateScalarType()
	{
		super(MetadataDate.class, Date.class);
	}


	@Override
	public MetadataDate getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataDate(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}


	@Override
	public String getCSharptType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDbType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getObjectiveCType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getJavaType()
	{
		return MetadataDate.class.getSimpleName();
	}

}
