/**
 * 
 */
package ecologylab.bigsemantics.metadata.scalar.types;

import java.awt.image.BufferedImage;

import ecologylab.bigsemantics.metadata.scalar.MetadataBufferedImage;
import ecologylab.serialization.ScalarUnmarshallingContext;

/**
 * @author Zachary O. Toups (zach@ecologylab.net)
 */
public class MetadataImageScalarType extends
		MetadataScalarType<MetadataBufferedImage, BufferedImage>
{

	public MetadataImageScalarType()
	{
		super(MetadataBufferedImage.class, BufferedImage.class, null, null);
	}

	@Override
	public MetadataBufferedImage getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		return null;
	}

}
