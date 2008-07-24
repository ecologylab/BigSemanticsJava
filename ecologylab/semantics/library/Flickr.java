package ecologylab.semantics.library;

import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.xml.xml_inherit;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Flickr extends Image
{
	@xml_nested MetadataString tags;
	@xml_nested MetadataString author;
	
	//Lazy evaluation for efficient retrieval.
	MetadataString tags()
	{
		MetadataString result = this.tags;
		if(result == null)
		{
			result 			= new MetadataString();
			this.tags 	= result;
		}
		return result;
	}
	
	MetadataString author()
	{
		MetadataString result = this.author;
		if(result == null)
		{
			result 			= new MetadataString();
			this.author 	= result;
		}
		return result;
	}
	
	public String getTags()
	{
		return tags().getValue();
	}
	
	public void hwSetTags(String tags)
	{
		setTags(tags);
		rebuildCompositeTermVector();
	}
	
	public String getAuthor()
	{
		return author().getValue();
	}
	
	public void hwSetAuthor(String author)
	{
		setAuthor(author);
		rebuildCompositeTermVector();
	}
	
	public void setTags(String tags)
	{
		this.tags().setValue(tags);
	}
	
	public void setAuthor(String author)
	{
		this.author().setValue(author);
	}
}
