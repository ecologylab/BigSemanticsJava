/**
 * 
 */
package ecologylab.semantics.metadata.builtins;

import java.util.HashSet;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.declarations.ClippingDeclaration;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * Mix-in for adding the context of a clipping to the description of a Document.
 * Example: Image, Video.
 * 
 * @author andruid
 */
@simpl_inherit
public class Clipping extends ClippingDeclaration
{
	
//	/**
//	 * Text connected to the clipping in the source document.
//	 */
//	@simpl_scalar
//	private MetadataString	context;
//
//	/**
//	 * Text connected to the clipping in the source document.
//	 */
//	// TODO use html context -- need methods to strip tags to set regular context from it.
//	@simpl_scalar
//	@simpl_hints(Hint.XML_LEAF_CDATA)
//	private MetadataString	contextHtml;
//
//	/**
//	 * Location of the clipping in the source document.
//	 */
//	@simpl_scalar
//	private MetadataString	xpath;
//
//	/**
//	 * The source document.
//	 */
//	@simpl_composite
//	@simpl_wrap
//	@mm_name("source_doc")
//	@simpl_scope(SemanticsNames.REPOSITORY_DOCUMENT_TRANSLATIONS)
//	private Document				sourceDoc;
//
//	/**
//	 * A hyperlinked Document.
//	 */
//	@simpl_composite
//	@mm_name("outlink")
//	@simpl_wrap
//	@simpl_scope(SemanticsNames.REPOSITORY_DOCUMENT_TRANSLATIONS)
//	private Document				outlink;

	private DocumentClosure	outlinkClosure;

	protected static int		numWithCaption;

	/**
	 * Total number of images we have created within this session
	 */
	static int							numConstructed;

	
	public Clipping()
	{
		numConstructed++;
	}
	
	public Clipping(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
		// NOTE is numConstructed++ missing? -- yin, 1/24/2012
	}
	
	public Clipping(MetaMetadataCompositeField metaMetadata, Document source)
	{
		this(metaMetadata);
		this.setSourceDoc(source);
	}
	
	public Clipping(MetaMetadataCompositeField metaMetadata, Document source, Document outlink, String context)
	{
		this(metaMetadata, source);
		initClipping(this, outlink, context);
	}

	public static void initClipping(Clipping clipping, Document outlink, String context)
	{
		if (outlink != null)
		{
			if (outlink.isDownloadDone())
			{
				clipping.setOutlink(outlink);
			}
			else
				clipping.outlinkClosure	= outlink.getOrConstructClosure();
		}
		if (context != null)
			clipping.setContext(context);
	}
	
	@Override
	public void serializationPreHook(TranslationContext translationContext)
	{
		if (outlinkClosure != null && !outlinkClosure.isRecycled())
			setOutlink(outlinkClosure.getDocument());
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
	
	/**
	 * used for deriving statistics that track how many images
	 * on the web have alt text.
	 * @return
	 */   
	public static int hasCaptionPercent()
	{
		return (int) (100.0f * numWithCaption / numConstructed);
	}

	public boolean isNullContext()
	{
		return getContextMetadata() == null || getContextMetadata().getValue() == null;
	}
	
	public boolean isNullXpath()
	{
		return getXpathMetadata() == null || getXpathMetadata().getValue() == null;
	}
	
	/**
	 * @return the outlinkContainer
	 */
	public DocumentClosure getOutlinkClosure()
	{
		return outlinkClosure;
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
	public final synchronized void recycle(boolean unconditional, HashSet<Metadata> visitedMetadata)
	{
		if (getSourceDoc() != null)
		{
			getSourceDoc().recycle();
			setSourceDoc(null);
		}
		if (getOutlink() != null)
		{
			getOutlink().recycle();
			setOutlink(null);
		}
		outlinkClosure	= null;
		super.recycle(visitedMetadata);
	}
	
	@Override
	public boolean hasLocation()
	{
		return getSourceDoc() != null && getSourceDoc().hasLocation();
	}
	
	@Override
	public ParsedURL getLocation()
	{
		return getSourceDoc() != null ? getSourceDoc().getLocation() : null;
	}

	@Override
	public boolean isClipping()
	{
		return true;
	}

}
