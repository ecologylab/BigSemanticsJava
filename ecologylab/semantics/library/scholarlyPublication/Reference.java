package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.ElementState.xml_nested;
import ecologylab.xml.types.element.Mappable;

/**
 * 
 * @author damaraju
 *
 */
public class Reference extends Metadata implements Mappable<ParsedURL>
{
//	@xml_attribute ParsedURL		link;
//	@xml_leaf String 				bibTex;
	
//	@xml_nested MetadataParsedURL		link 			= new MetadataParsedURL();
//	@xml_nested MetadataString			bibTex 			= new MetadataString();

	@xml_nested MetadataParsedURL		link;
	@xml_nested MetadataString			bibTex;
	public Reference()
	{
		// TODO Auto-generated constructor stub
	}
	public Reference(ParsedURL link)
	{
//		this.link = link;
		this.link.setValue(link);
	}
	public Reference(String link)
	{
//		this.link = ParsedURL.getAbsolute(link);
		this.link.setValue(ParsedURL.getAbsolute(link));
	}

	MetadataString bibTex()
	{
		MetadataString result = this.bibTex;
		if(result == null)
		{
			result 			= new MetadataString();
			this.bibTex 	= result;
		}
		return result;
	}
	
	MetadataParsedURL link()
	{
		MetadataParsedURL result = this.link;
		if(result == null)
		{
			result 			= new MetadataParsedURL();
			this.link 	= result;
		}
		return result;
	}
	
	public ParsedURL key()
	{
//		return link;
		return link().getValue();
	}
}
