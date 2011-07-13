/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.semantics.metadata.scalar.MetadataStringBuilder;
import ecologylab.serialization.ScalarUnmarshallingContext;

/**
 * @author andruid
 *
 */
public class MetadataStringBuilderScalarType extends
		MetadataScalarType<MetadataStringBuilder, StringBuilder>
{

	public MetadataStringBuilderScalarType()
	{
		super(MetadataStringBuilder.class, StringBuilder.class);
	}

	@Override
	public MetadataStringBuilder getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataStringBuilder(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

	@Override
	public String getCSharptType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getJavaType() {
		return MetadataStringBuilder.class.getSimpleName();
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

}
