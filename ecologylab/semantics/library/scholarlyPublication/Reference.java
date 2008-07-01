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
	@xml_nested MetadataParsedURL		link;
	@xml_nested MetadataString			bibTex;
	public Reference()
	{
	}
	
	public Reference(ParsedURL link)
	{
		this.link.setValue(link);
	}
	
	public Reference(String link)
	{
		this.link.setValue(ParsedURL.getAbsolute(link));
	}

	//Lazy evaluation.
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
		return link().getValue();
	}
}
