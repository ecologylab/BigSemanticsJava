/**
 * 
 */
package ecologylab.semantics.metadata.scalar;

import java.awt.image.BufferedImage;

import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.NullTermVector;

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
