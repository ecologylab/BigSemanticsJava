package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.model.text.SemanticTextChunk;
import ecologylab.serialization.simpl_inherit;

/**
 * Text clippings from documents.
 **/

@simpl_inherit
public class TextClipping extends Clipping
{

	@simpl_scalar
	private MetadataString	text;

	/**
	 * Constructor
	 **/

	public TextClipping()
	{
		super();
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
	 * Constructor
	 **/

	public TextClipping(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
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

	/**
	 * Gets the value of the field text
	 **/

	public String getText()
	{
		return text().getValue();
	}

	/**
	 * Sets the value of the field text
	 **/

	public void setText(String text)
	{
		this.text().setValue(text);
	}

	/**
	 * The heavy weight setter method for field text
	 **/

	public void hwSetText(String text)
	{
		this.text().setValue(text);
		rebuildCompositeTermVector();
	}

	/**
	 * Tests to see if the value of the field is null, or if the field itself is null: text
	 **/

	public boolean isNullText()
	{
		return text == null || text.getValue() == null;
	}

	/**
	 * Sets the text directly
	 **/

	public void setTextMetadata(MetadataString text)
	{
		this.text = text;
	}

	public void setText(CharSequence textSequence)
	{
		this.text	= new MetadataString(textSequence.toString());
	}
	/**
	 * Heavy Weight Direct setter method for text
	 **/

	public void hwSetTextMetadata(MetadataString text)
	{
		if (this.text != null && this.text.getValue() != null && hasTermVector())
			termVector().remove(this.text.termVector());
		this.text = text;
		rebuildCompositeTermVector();
	}

}
