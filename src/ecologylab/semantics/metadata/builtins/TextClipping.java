package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.builtins.declarations.TextClippingDeclaration;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.model.text.SemanticTextChunk;
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

	/**
	 * The heavy weight setter method for field text
	 **/

	public void hwSetText(String text)
	{
		this.text().setValue(text);
		rebuildCompositeTermVector();
	}

	public void setText(CharSequence textSequence)
	{
		setTextMetadata(new MetadataString(textSequence.toString()));
	}
	
	/**
	 * Heavy Weight Direct setter method for text
	 **/
	public void hwSetTextMetadata(MetadataString text)
	{
		if (getText() != null && hasTermVector())
			termVector().remove(this.getTextMetadata().termVector());
		this.setTextMetadata(text);
		rebuildCompositeTermVector();
	}

}
