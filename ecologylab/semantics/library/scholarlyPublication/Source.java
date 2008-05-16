package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.ElementState.xml_nested;


/**
 * 
 * @author damaraju
 *
 */
public class Source extends Metadata
{

//	@xml_leaf String		heading;
//	@xml_leaf ParsedURL		heading;
//	@xml_leaf ParsedURL		tableOfContents;
//	@xml_leaf String		pages;
//	@xml_leaf String		yearOfPublication;
//	@xml_leaf String		isbn;	
//	@xml_attribute ParsedURL	imgPurl;
	
//	@xml_nested MetadataParsedURL		archive 			= new MetadataParsedURL();
//	@xml_nested MetadataString		heading 			= new MetadataString();
//	@xml_nested MetadataParsedURL	table_of_contents 	= new MetadataParsedURL();
//	@xml_nested MetadataString		pages 				= new MetadataString();
//	@xml_nested MetadataString		year_of_publication 	= new MetadataString();
//	@xml_nested MetadataString		isbn 				= new MetadataString();
//	@xml_nested MetadataParsedURL	imgPurl 			= new MetadataParsedURL();
	
	@xml_nested MetadataParsedURL		archive;
	@xml_nested MetadataString			heading;
	@xml_nested MetadataParsedURL		table_of_contents;
	@xml_nested MetadataString			pages;
	@xml_nested MetadataString			year_of_publication;
	@xml_nested MetadataString			isbn;
	@xml_nested MetadataParsedURL		imgPurl; 
	
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
		MetadataParsedURL result = this.table_of_contents;
		if(result == null)
		{
			result 			= new MetadataParsedURL();
			this.table_of_contents 	= result;
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
	MetadataString year_of_publication()
	{
		MetadataString result = this.year_of_publication;
		if(result == null)
		{
			result 			= new MetadataString();
			this.year_of_publication 	= result;
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
//		return imgPurl;
		return imgPurl().getValue();
	}
	/**
	 * @param imgPurl the imgPurl to set
	 */
	public void setImgPurl(ParsedURL imgPurl)
	{
//		this.imgPurl = imgPurl;
		this.imgPurl().setValue(imgPurl);
	}

	

}
