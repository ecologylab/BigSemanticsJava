/**
 * 
 */
package ecologylab.bigsemantics.metadata.scalar;

import ecologylab.bigsemantics.model.text.ITermVector;
import ecologylab.bigsemantics.model.text.NullTermVector;

/**
 * @author andruid
 *
 */
public class MetadataDouble extends MetadataScalarBase<Double>
{

	/**
	 * 
	 */
	public MetadataDouble()
	{
		
	}

	/**
	 * @param value
	 */
	public MetadataDouble(Double value)
	{
		super(value);
		}

	@Override
	public ITermVector termVector()
	{
		return NullTermVector.singleton();
	}

}
