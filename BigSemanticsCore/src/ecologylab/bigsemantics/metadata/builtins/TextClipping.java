package ecologylab.bigsemantics.metadata.builtins;

import ecologylab.bigsemantics.metadata.builtins.declarations.TextClippingDeclaration;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.model.text.SemanticTextChunk;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * Text clippings from documents.
 **/

@simpl_inherit
public class TextClipping extends TextClippingDeclaration implements TextualMetadata
{

//	@simpl_scalar
//	@mm_name("text")
//	private MetadataString	text;

	public TextClipping()
	{
		super();
	}
	
	public TextClipping(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
	}

	public TextClipping(String context)
	{
		super();
		setContext(context);
	}

	public TextClipping(String context, boolean isSemanticText)
	{
		this(context);

		SemanticTextChunk chunk 				= new SemanticTextChunk(context);			
		SemanticTextChunk trimmedChunk	= (SemanticTextChunk) chunk.trimPhatChunk(isSemanticText);
		setText(trimmedChunk.string());
		chunk.recycle();
		trimmedChunk.recycle();
	}

	@Override
	public void setText(String newText)
	{
		HtmlText media = getMedia();
		if (media != null)
		{
		  media.setText(newText);
		}
	}

	@Override
	public String getText()
	{
		HtmlText media = getMedia();
		if (media != null)
		{
  		return media.getText();
		}
		return "";
	}

	/**
	 * The heavy weight setter method for field text
	 **/

	public void hwSetText(String text)
	{
		HtmlText media = getMedia();
		if (media != null)
		{
  		media.text().setValue(text);
  		rebuildCompositeTermVector();
		}
	}

	public void setText(CharSequence textSequence)
	{
		HtmlText media = getMedia();
		if (media != null)
		{
  		media.setTextMetadata(new MetadataString(textSequence.toString()));
		}
	}
	
	/**
	 * Heavy Weight Direct setter method for text
	 **/
	public void hwSetTextMetadata(MetadataString text)
	{
		HtmlText media = getMedia();
		if (media != null)
		{
  		if (getText() != null && hasTermVector())
  			termVector().remove(media.getTextMetadata().termVector());
  		media.setTextMetadata(text);
  		rebuildCompositeTermVector();
		}
	}

}
