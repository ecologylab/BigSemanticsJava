/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import ecologylab.semantics.metadata.scalar.MetadataInteger;
import ecologylab.semantics.metadata.scalar.MetadataStringBuilder;
import ecologylab.xml.ScalarUnmarshallingContext;

/**
 * @author andruid
 *
 */
public class MetadataStringBuilderScalarType extends
		MetadataScalarScalarType<MetadataStringBuilder, StringBuilder>
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

}
