/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.util.Date;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.semantics.metadata.builtins.declarations.AnnotationDeclaration;
import ecologylab.semantics.metadata.scalar.MetadataDate;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * @author andrew
 *
 */
@simpl_inherit
public class Annotation extends AnnotationDeclaration implements TextualMetadata
{
	
//	@mm_name("text")
//	@simpl_scalar
//	MetadataString text;
//	
//	@mm_name("author")
//	@simpl_scalar
//	MetadataString author;
//	
//	@mm_name("creation_time")
//	@simpl_scalar
//	MetadataDate creationTime;
	
	public Annotation()
	{
		super();
	}
	
	public Annotation(MetaMetadataCompositeField mmd)
	{
		super(mmd);
	}
	
	public Annotation(MetaMetadataCompositeField metaMetadata, String text)
	{
		this(metaMetadata);
		this.setText(text);
		this.setAuthor(PropertiesAndDirectories.userName());
		this.setCreationTimeMetadata(new MetadataDate(new Date()));
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
	
	@Override 
	public String toString()
	{
		return super.toString() + "\r\ntext: " + this.getText();
	}
	
}
