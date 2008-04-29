package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.types.element.Mappable;

/**
 * 
 * @author damaraju
 *
 */
public class Reference extends Metadata implements Mappable<ParsedURL>
{
	@xml_attribute ParsedURL		link;
	@xml_leaf String 				bibTex;

	public Reference()
	{
		// TODO Auto-generated constructor stub
	}
	public Reference(ParsedURL link)
	{
		this.link = link;
	}
	public Reference(String link)
	{
		this.link = ParsedURL.getAbsolute(link);
	}

	public ParsedURL key()
	{
		return link;
	}
}
