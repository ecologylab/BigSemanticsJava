/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.semantics.metadata.scalar.MetadataInteger;
import ecologylab.serialization.ScalarUnmarshallingContext;

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
	public String getJavaType()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
