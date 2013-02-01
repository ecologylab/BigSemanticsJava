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
}
