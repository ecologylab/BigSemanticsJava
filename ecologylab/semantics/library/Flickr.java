package ecologylab.semantics.library;

import ecologylab.model.MetadataField;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Flickr extends Image
{
//	@xml_nested MetadataString tags = new MetadataString();
//	@xml_nested MetadataString author = new MetadataString();
	
	@xml_nested MetadataString tags;
	@xml_nested MetadataString author;
	
//	@xml_attribute String	tags;
//	@xml_attribute String	author;
	
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
//		return tags;
		return tags().getValue();
	}
	public void hwSetTags(String tags)
	{
//		this.tags = tags;
		this.tags().setValue(tags);
		rebuildCompositeTermVector();
	}
	public String getAuthor()
	{
//		return author;
		return author().getValue();
	}
	public void hwSetAuthor(String author)
	{
//		this.author = author;
		this.author().setValue(author);
		rebuildCompositeTermVector();
	}
	public void setTags(String tags)
	{
//		this.tags = tags;
		this.tags().setValue(tags);
	}
	public void setAuthor(String author)
	{
//		this.author = author;
		this.author().setValue(author);
	}
}
