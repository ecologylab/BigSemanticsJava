/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.util.HashSet;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.Hint;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.simpl_scope;

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
	@simpl_wrap
	@mm_name("source")
	@simpl_scope(SemanticsNames.REPOSITORY_DOCUMENT_TRANSLATIONS)
	private Document				source;
	
	/**
	 * A hyperlinked Document.
	 */
	@simpl_composite
	@mm_name("outlink") 
	@simpl_wrap
	@simpl_scope(SemanticsNames.REPOSITORY_DOCUMENT_TRANSLATIONS)
	private Document				outlink;
	
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
	public Clipping(MetaMetadataCompositeField metaMetadata, Document source)
	{
		this(metaMetadata);
		this.source	= source;;
	}
	public Clipping(MetaMetadataCompositeField metaMetadata, Document source, Document outlink, String context)
	{
		this(metaMetadata, source);
		if (outlink != null)
		{
			if (outlink.isDownloadDone())
			{
				this.outlink				= outlink;
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
			outlink	= outlinkClosure.getDocument();
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
		return source;
	}

	/**
	 * @return the outlink
	 */
	public Document getOutlink()
	{
		return outlink;
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
	public final synchronized void recycle (boolean unconditional, HashSet<Metadata> visitedMetadata)
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
		super.recycle(visitedMetadata);
	}
	
	public void setSource(Document source)
	{
		this.source	= source;
//		if (this.source == null)
//			this.source	= new DocumentMetadataWrap(source);
//		else
//			this.source.setDocument(source);
	}
	public void setOutlink(Document outlink)
	{
		this.outlink	= outlink;
//		if (this.outlink == null)
//			this.outlink	= new DocumentMetadataWrap(outlink);
//		else
//			this.outlink.setDocument(outlink);
	}

}
