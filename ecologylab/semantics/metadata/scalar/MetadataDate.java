/**
 * 
 */
package ecologylab.semantics.metadata.scalar;

import java.util.Date;

import ecologylab.semantics.model.text.NullTermVector;

/**
 * @author andruid
 *
 */
public class MetadataDate extends MetadataScalarBase<Date>
{
	public MetadataDate()
	{
	}
	
	public MetadataDate(Date value)
	{
		super(value);
	}
	
	@Override
	public NullTermVector termVector() 
	{
		return NullTermVector.singleton();
	}
	

}
