package ecologylab.semantics.metametadata;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Example url for a meta-metadata wrapper, with comments and potentially other information.
 * 
 * @author quyin
 *
 */
@simpl_inherit
public class ExampleUrl
{
	
	@simpl_scalar
	private ParsedURL url;
	
	@simpl_scalar
	private String		comment;

	public ParsedURL getUrl()
	{
		return url;
	}

	public void setUrl(ParsedURL url)
	{
		this.url = url;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

}
