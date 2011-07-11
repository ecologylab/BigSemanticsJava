/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.Hint;
import ecologylab.serialization.simpl_inherit;

/**
 * Mix-in for adding the context of a clipping to the description of a Document.
 * Example: Image, Video.
 * 
 * @author andruid
 */
@simpl_inherit
public class Clipping extends Metadata
{
	/**
	 * Text connected to the clipping in the source document.
	 */
	@simpl_scalar
	private MetadataString	context;

	/**
	 * Text connected to the clipping in the source document.
	 */
	//TODO use html context -- need methods to strip tags to set regular context from it.
	@simpl_scalar @simpl_hints(Hint.XML_LEAF_CDATA)
	private MetadataString	contextHtml;

	/**
	 * Location of the clipping in the source document.
	 */
	@simpl_scalar
	private MetadataString	xpath;

	/**
	 * The source document.
	 */
	@simpl_composite
	@mm_name("source") 
	private DocumentMetadataWrap				source;
	
	/**
	 * A hyperlinked Document.
	 */
	@simpl_composite
	@mm_name("outlink") 
	private DocumentMetadataWrap				outlink;
	
	private DocumentClosure				outlinkClosure;

	protected static int							numWithCaption;
	/**
	 * Total number of images we have created within this session
	 */
	static int							numConstructed;

	
	/**
	 * 
	 */
	public Clipping()
	{
		numConstructed++;
	}
	public Clipping(Document source)
	{
		this();
		this.source	= new DocumentMetadataWrap(source);
	}
	public Clipping(Document source, Document outlink, String context)
	{
		this(source);
		if (outlink != null)
		{
			if (outlink.isDownloadDone())
			{
				this.outlink				= new DocumentMetadataWrap(outlink);
			}
			else
				this.outlinkClosure	= outlink.getOrConstructClosure();
		}
		if (context != null)
			setContext(context);
	}
	/**
	 * @param metaMetadata
	 */
	public Clipping(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
	}
	
	@Override
	protected void serializationPreHook()
	{
		if (outlinkClosure != null && !outlinkClosure.isRecycled())
			outlink	= new DocumentMetadataWrap(outlinkClosure.getDocument());
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

	public String getContext()
	{
		return context == null ? null : context.getValue();
	}

	/**
	 * Sets the value of the field context
	 **/

	public void setContext(String context)
	{
		this.context().setValue(context);
	}

	/**
	 * The heavy weight setter method for field context
	 **/

	public void hwSetContext(String context)
	{
		this.context().setValue(context);
		rebuildCompositeTermVector();
	}

	
	/**
	 * used for deriving statistics that track how many images
	 * on the web have alt text.
	 * @return
	 */   
	public static int hasCaptionPercent()
	{
		return (int) (100.0f * (float) numWithCaption / ((float) numConstructed));
	}

	public boolean isImage()
	{
		return false;
	}

	public boolean isNullContext()
	{
		return context == null || context.getValue() == null;
	}
	
	public boolean isNullXpath()
	{
		return xpath == null || xpath.getValue() == null;
	}
	
	public MetadataString xpath()
	{
		MetadataString result = this.xpath;
		if (result == null)
		{
			result = new MetadataString();
			this.xpath = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field context
	 **/

	public String getXpath()
	{
		return xpath == null ? null : xpath().getValue();
	}

	/**
	 * Sets the value of the field context
	 **/

	public void setXpath(String context)
	{
		this.xpath().setValue(context);
	}

	/**
	 * @return the outlinkContainer
	 */
	public DocumentClosure getOutlinkClosure()
	{
		return outlinkClosure;
	}

	/**
	 * @return the contextHtml
	 */
	public MetadataString getContextHtml()
	{
		return contextHtml;
	}

	/**
	 * @return the source
	 */
	public Document getSource()
	{
		return source.getDocument();
	}

	/**
	 * @return the outlink
	 */
	public Document getOutlink()
	{
		return outlink.getDocument();
	}

	/**
	 * @return the numWithCaption
	 */
	public static int getNumWithCaption()
	{
		return numWithCaption;
	}

	/**
	 * @return the numConstructed
	 */
	public static int getNumConstructed()
	{
		return numConstructed;
	}

	/**
	 * Called to free resources associated with this MediaElement. Removes references to this from
	 * the associated <code>Container</code>, as well as freeing resources directly associated (such
	 * as pixel buffers).
	 * <p>
	 * Checks to make sure that element is not on screen before it does anything. Then, calls
	 * {@link #doRecycle() doRecycle()}. That is the method that really frees resources. It is the
	 * one that derived classes need to override. This is why the routine is being declared final.
	 */
	public final synchronized void recycle (boolean unconditional )
	{
		if (source != null)
		{
			source.recycle();
			source	= null;
		}
		if (outlink != null)
		{
			outlink.recycle();
			outlink	= null;
		}
		outlinkClosure	= null;
		super.recycle();
	}

}
