/**
 * 
 */
package ecologylab.semantics.metadata.scalar;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.semantics_pseudo_scalar;
import ecologylab.semantics.model.text.NullTermVector;
import ecologylab.xml.simpl_inherit;

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
}
