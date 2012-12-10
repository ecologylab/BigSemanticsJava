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
		super(MetadataStringBuilder.class, StringBuilder.class, null, null);
	}

	@Override
	public MetadataStringBuilder getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataStringBuilder(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

}
