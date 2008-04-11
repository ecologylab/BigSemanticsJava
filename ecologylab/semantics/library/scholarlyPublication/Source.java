package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;


/**
 * 
 * @author damaraju
 *
 */
public class Source extends Metadata
{

	@xml_leaf String		heading;
	@xml_leaf ParsedURL		archive;
	@xml_leaf ParsedURL		tableOfContents;
	@xml_leaf String		pages;
	@xml_leaf String		yearOfPublication;
	@xml_leaf String		isbn;	
	@xml_attribute ParsedURL												imgPurl;
	/**
	 * @return the imgPurl
	 */
	public ParsedURL getImgPurl()
	{
		return imgPurl;
	}
	/**
	 * @param imgPurl the imgPurl to set
	 */
	public void setImgPurl(ParsedURL imgPurl)
	{
		this.imgPurl = imgPurl;
	}

	

}
