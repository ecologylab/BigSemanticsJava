/**
 * 
 */
package ecologylab.semantics.metadata.scalar.types;

import java.io.File;

import ecologylab.semantics.metadata.scalar.MetadataFile;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.TranslationContext;

/**
 * @author andrew
 *
 */
public class MetadataFileScalarType extends MetadataScalarType<MetadataFile, File>
{

	public MetadataFileScalarType()
	{
		super(MetadataFile.class, File.class, null, null);
	}

	@Override
	public MetadataFile getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return new MetadataFile(getValueInstance(value, formatStrings, scalarUnmarshallingContext));
	}

	
	@Override
	public String marshall(MetadataFile instance, TranslationContext serializationContext)
	{
		return operativeScalarType().marshall(instance.getValue(), serializationContext);
	}
	
}
