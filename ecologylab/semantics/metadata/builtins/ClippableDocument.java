/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.Metadata.mm_name;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.ElementState.simpl_scalar;

/**
 * Base class for Document subtypes that function as clippings, that is, that can meaningfully be 
 * embedded in source documents.
 * Adds contextual information from the source document to the metadata description.
 * 
 * @author andruid
 *
 */
public class ClippableDocument extends Document
{
	/**
	 * Text connected to the clipping in the source document.
	 */
	@simpl_scalar
	private MetadataString	context;

	/**
	 * Explicit description of the clipped media in the document.
	 */
	@mm_name("caption") 
	@simpl_scalar private
	MetadataString					caption;


	/**
	 * 
	 */
	public ClippableDocument()
	{
	}

	/**
	 * @param metaMetadata
	 */
	public ClippableDocument(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
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

	/**
	 * The heavy weight setter method for field context
	 **/

	@Override
	public void hwSetContext(String context)
	{
		this.context().setValue(context);
		rebuildCompositeTermVector();
	}

	MetadataString caption()
	{
		MetadataString result = this.caption;
		if (result == null)
		{
			result = new MetadataString();
			this.caption = result;
		}
		return result;
	}

	public String getCaption()
	{
		// return caption;
		return caption().getValue();
	}

	public void setCaption(String caption)
	{
		// this.caption = caption;
		this.caption().setValue(caption);
	}
	
	public void hwSetCaption(String caption)
	{
		// this.caption = caption;
		this.setCaption(caption);
		rebuildCompositeTermVector();
	}

	public boolean isNullCaption()
	{
		return caption == null || caption.getValue() == null;
	}

	
}
