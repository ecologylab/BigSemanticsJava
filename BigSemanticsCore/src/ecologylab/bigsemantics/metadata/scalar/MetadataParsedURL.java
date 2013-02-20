/**
 * 
 */
package ecologylab.bigsemantics.metadata.scalar;

import ecologylab.bigsemantics.metadata.semantics_pseudo_scalar;
import ecologylab.bigsemantics.model.text.NullTermVector;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * @author bharat
 *
 */
@simpl_inherit
@semantics_pseudo_scalar
public class MetadataParsedURL extends MetadataScalarBase<ParsedURL>
{
	public MetadataParsedURL()
	{
	}

	public MetadataParsedURL(ParsedURL purl)
	{
		super(purl);
	}

	@Override
	public NullTermVector termVector() 
	{
		return NullTermVector.singleton();
	}
	
  public int hashCode()
  {
  	return value == null ? 0 : value.hashCode();
  }
  
  public boolean equals(Object otherPurl)
  {
  	if (otherPurl != null && otherPurl instanceof MetadataParsedURL)
  	{
  		if (value != null && ((MetadataParsedURL) otherPurl).value != null)
  		  return value.equals(((MetadataParsedURL) otherPurl).value);
  		if (value == null && ((MetadataParsedURL) otherPurl).value == null)
  			return true;
  	}
  	return false;
  }
  
}
