package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scalar.MetadataInteger;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.xml.ElementState.xml_nested;


/**
 * 
 * @author damaraju
 *
 */
public class Source extends MetadataBase
{
	@xml_nested MetadataParsedURL		archive;
	@xml_nested MetadataString			heading;
	@xml_nested MetadataParsedURL		tableOfContents;
	@xml_nested MetadataString			pages;
//	@xml_nested MetadataString			yearOfPublication;/*year_of_publication*/
	@xml_nested MetadataInteger			yearOfPublication;
	@xml_nested MetadataString			isbn;
	@xml_nested MetadataParsedURL		imgPurl; 
	
	//Lazy evaluationf or efficient reterival.
	MetadataParsedURL archive()
	{
		MetadataParsedURL result = this.archive;
		if(result == null)
		{
			result 			= new MetadataParsedURL();
			this.archive 	= result;
		}
		return result;
	}
	MetadataString heading()
	{
		MetadataString result = this.heading;
		if(result == null)
		{
			result 			= new MetadataString();
			this.heading 	= result;
		}
		return result;
	}
	
	MetadataParsedURL table_of_contents()
	{
		MetadataParsedURL result = this.tableOfContents;
		if(result == null)
		{
			result 			= new MetadataParsedURL();
			this.tableOfContents 	= result;
		}
		return result;
	}
	
	MetadataString pages()
	{
		MetadataString result = this.pages;
		if(result == null)
		{
			result 			= new MetadataString();
			this.pages 	= result;
		}
		return result;
	}
	
	MetadataInteger year_of_publication()
	{
		MetadataInteger result = this.yearOfPublication;
		if(result == null)
		{
			result 			= new MetadataInteger();
			this.yearOfPublication 	= result;
		}
		return result;
	}
	
	MetadataString isbn()
	{
		MetadataString result = this.isbn;
		if(result == null)
		{
			result 			= new MetadataString();
			this.isbn 	= result;
		}
		return result;
	}
	
	MetadataParsedURL imgPurl()
	{
		MetadataParsedURL result = this.imgPurl;
		if(result == null)
		{
			result 			= new MetadataParsedURL();
			this.imgPurl 	= result;
		}
		return result;
	}
	
	/**
	 * @return the imgPurl
	 */
	public ParsedURL getImgPurl()
	{
		return imgPurl().getValue();
	}
	
	/**
	 * @param imgPurl the imgPurl to set
	 */
	public void setImgPurl(ParsedURL imgPurl)
	{
		this.imgPurl().setValue(imgPurl);
	}
}
