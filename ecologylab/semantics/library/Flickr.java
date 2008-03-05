package ecologylab.semantics.library;

import ecologylab.xml.xml_inherit;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Flickr extends Image
{
	@xml_attribute String	tags;
	@xml_attribute String	author;
	
	public String getTags()
	{
		return tags;
	}
	public void setTags(String tags)
	{
		this.tags = tags;
	}
	public String getAuthor()
	{
		return author;
	}
	public void setAuthor(String author)
	{
		this.author = author;
	}
}
