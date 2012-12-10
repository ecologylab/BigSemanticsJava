/**
 * 
 */
package ecologylab.semantics.metadata.scalar;

import java.io.File;

import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.NullTermVector;

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
