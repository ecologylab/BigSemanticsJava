/**
 * 
 */
package ecologylab.bigsemantics.metadata.scalar;

import java.awt.image.BufferedImage;

import ecologylab.bigsemantics.model.text.ITermVector;
import ecologylab.bigsemantics.model.text.NullTermVector;

/**
 * @author Zachary O. Toups (zach@ecologylab.net)
 */
public class MetadataBufferedImage extends MetadataScalarBase<BufferedImage>
{

	@Override
	public ITermVector termVector()
	{
		return NullTermVector.singleton();
	}

}
