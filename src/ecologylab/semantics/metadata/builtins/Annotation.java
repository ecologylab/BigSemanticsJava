/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.util.Date;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.mm_name;
import ecologylab.semantics.metadata.scalar.MetadataDate;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * @author andrew
 *
 */
@simpl_inherit
public class Annotation extends Metadata implements TextualMetadata
{
	@mm_name("text")
	@simpl_scalar
	MetadataString text;
	
	@mm_name("author")
	@simpl_scalar
	MetadataString author;
	
	@mm_name("creation_time")
	@simpl_scalar
	MetadataDate creationTime;
	
	public Annotation()
	{
		
	}
	
	public Annotation(MetaMetadataCompositeField metaMetadata, String text)
	{
		this(metaMetadata);
		this.setText(text);
		this.setAuthor(PropertiesAndDirectories.userName());
		this.creationTime = new MetadataDate(new Date());
		this.rebuildCompositeTermVector();
	}
	
	public Annotation(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
	}

	public MetadataString getAuthorMetadata()
	{
		return author;
	}

	public void setAuthorMetadata(MetadataString author)
	{
		this.author = author;
	}

	public MetadataDate getCreationTimeMetadata()
	{
		return creationTime;
	}

	public void setCreationTimeMetadata(MetadataDate creationTime)
	{
		this.creationTime = creationTime;
	}

	public MetadataString getTextMetadata()
	{
		return text;
	}
	
	public void setTextMetadata(MetadataString text)
	{
		this.text = text;
		this.rebuildCompositeTermVector();
	}

	@Override
	public void setText(String newText)
	{
		this.text().setValue(newText);
		this.rebuildCompositeTermVector();
	}

	@Override
	public String getText()
	{
		return text().getValue();
	}
	
	/**
	 * Lazy Evaluation for text
	 **/

	public MetadataString text()
	{
		MetadataString result = this.text;
		if (result == null)
		{
			result = new MetadataString();
			this.text = result;
		}
		return result;
	}
	
	public void setAuthor(String authorName)
	{
		author().setValue(authorName);
	}
	
	public MetadataString author()
	{
		MetadataString result = this.author;
		if (result == null)
		{
			result = new MetadataString();
			this.author = result;
		}
		return result;
	}
}
