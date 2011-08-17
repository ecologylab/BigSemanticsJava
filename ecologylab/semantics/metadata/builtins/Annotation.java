/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.util.Date;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataDate;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.simpl_inherit;

/**
 * @author andrew
 *
 */
@simpl_inherit
public class Annotation extends Metadata implements TextualMetadata
{
	@simpl_scalar
	MetadataString text;
	
	@simpl_scalar
	MetadataString author;
	
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
	}

	@Override
	public void setText(String newText)
	{
		this.text().setValue(newText);
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
