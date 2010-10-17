package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.simpl_inherit;

/**
 * Text clippings from documents.
 **/

@simpl_inherit
public class Text extends Metadata
{

	@simpl_scalar
	private MetadataString	text;

	/**
	 * Text connected to the clipping in the source document.
	 */
	@simpl_scalar
	private MetadataString	context;

	/**
	 * Constructor
	 **/

	public Text()
	{
		super();
	}

	/**
	 * Constructor
	 **/

	public Text(MetaMetadataCompositeField metaMetadata)
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
	
	public MetadataString context()
	{
		MetadataString result = this.context;
		if (result == null)
		{
			result = new MetadataString();
			this.context = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field context
	 **/

	@Override
	public String getContext()
	{
		return context().getValue();
	}

	/**
	 * Sets the value of the field context
	 **/

	@Override
	public void setContext(String context)
	{
		this.context().setValue(context);
	}

	@Override
	public void hwSetContext(String context)
	{
		this.context().setValue(context);
		rebuildCompositeTermVector();
	}

}
