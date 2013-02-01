/**
 * 
 */
package ecologylab.bigsemantics.metadata.scalar;

import java.io.File;

import ecologylab.bigsemantics.model.text.ITermVector;
import ecologylab.bigsemantics.model.text.NullTermVector;

/**
 * @author andrew
 *
 */
public class MetadataFile extends MetadataScalarBase<File>
{
	
	public MetadataFile()
	{
	}
	
	public MetadataFile(File value)
	{
		super(value);
	}

	@Override
	public ITermVector termVector()
	{
		return NullTermVector.singleton();
	}

}
