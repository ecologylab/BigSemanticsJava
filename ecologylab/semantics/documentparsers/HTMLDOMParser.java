package ecologylab.semantics.documentparsers;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.tidy.DOMNodeImpl;
import org.w3c.tidy.TdNode;
import org.w3c.tidy.Tidy;

import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.semantics.html.documentstructure.LinkType;
import ecologylab.semantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.documentstructure.SemanticInLinks;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.ImageClipping;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.old.AbstractImgElement;
import ecologylab.semantics.old.OldContainerI;
import ecologylab.serialization.XMLTools;

/**
 * Parse HTML page and create DOM
 * 
 * @author eunyee
 * 
 */
public class HTMLDOMParser
		extends HTMLParserCommon
{

	/**
	 * Root DOM of the current document
	 */
	private org.w3c.dom.Document	dom;

	protected Tidy								tidy	= new Tidy();

	public HTMLDOMParser(NewInfoCollector infoCollector)
	{
		super(infoCollector);
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
	}

	protected org.w3c.dom.Document getDom()
	{
		org.w3c.dom.Document result = this.dom;
		if (result == null)
		{
			result = createDom();
			this.dom = result;
		}
		return result;
	}

	/**
	 * 
	 * @return A bogus dom with a root element called empty.
	 */
	protected org.w3c.dom.Document createDom()
	{
		return tidy.parseDOM(inputStream(), /* System.out */null);
	}

	/**
	 * 
	 * @return The root node of the document, which should be <html>.
	 */
	public TdNode getRootNode()
	{
		return ((DOMNodeImpl) getDom()).adaptee;
	}

	/**
	 * Andruid says: NEVER override this method when you parse HTML. Instead, override postParse().
	 */
	@Override
	public final Document parse()
	{
		Document result	= null;
		try
		{
			// we dont build a tidy for for direct binding
			// Uncomment System.out to print parsed page to console.
			// if(metaMetadata == null || !"direct".equals(metaMetadata.getParser()))
			// document = tidy.parseDOM(inputStream(), /*System.out*/ null );
			// getDom();

			// SimpleTimer.get("fetching_and_rendering.log").finishTiming(getContainer());

			doParse();
		}
		catch (Exception e)
		{
			debug("ERROR: while parsing document - " + getDocument().location());
			e.printStackTrace();
		}
		finally
		{
			recycle();
		}
		return null;
	}

	protected Document doParse()
	{
		return null;
	}

	public void recycle()
	{
		dom = null;
		tidy = null;
		super.recycle();
	}

	/**
	 * create image and text surrogates for this HTML document, and add these surrogates into the
	 * localCollection in Container.
	 */
	public ImageClipping newImgTxt(ImgElement imgNode, ParsedURL anchorHref)
	{
		String alt = imgNode.getAlt();
		int width = imgNode.getWidth();
		int height = imgNode.getHeight();
		boolean isMap = imgNode.isMap();

		ParsedURL srcPurl = imgNode.getSrc();

		if (anchorHref != null)
			processHref(anchorHref);

		ImageClipping result	= null;
		if (srcPurl != null)
		{
			String textContext = imgNode.getTextContext();
			if (anchorHref != null)
				currentAnchorHref = anchorHref;

			if (alt != null)
				alt = alt.trim();
			
			Image image				= infoCollector.getOrConstructImage(srcPurl);
			Document outlink	= infoCollector.getOrConstructDocument(anchorHref);
			result						= image.constructClippingCandidate(getDocument(), outlink, imgNode.getAlt(), imgNode.getTextContext());
		}

		// Reset the currentAnchorHref
		currentAnchorHref = null;

		return result;
	}

	@Override
	public void newAHref(HashMap<String, String> attributesMap)
	{
		super.newAHref(attributesMap);
	}

	/**
	 * Called when the parser see's the <code>&lt;title&gt; tag.
	 */
	public void setTitle(TdNode titleNode)
	{
		StringBuilder title = null;
		for (TdNode node = titleNode.content(); node != null; node = node.next())
		{
			if (node.type == TdNode.TextNode)
			{
				title = StringBuilderUtils.trimAndDecodeUTF8(title, node, 0, true);
				if (title != null)
				{
					XMLTools.unescapeXML(title);
					getDocument().hwSetTitle(StringTools.toString(title));
					StringBuilderUtils.release(title);
				}
				break;
			}
		}
	}

	/**
	 * Create TextElement and add to the localCollection in Container.
	 */
	// TODO -- take a BtringBuilder instead of a String. and use if efficiently!!!!!!
	public void newTxt(ParagraphText paraText)
	{
		if ((paraText != null) && (paraText.length() > 0) && (container != null))
		{
			// container.createTextElementAndAddToCollections(contextChunk, contextString, userRequested)
			container.createTextFromPhatContextAddToCollections(paraText);
		}
	}

	public int numCandidatesExtractedFrom()
	{
		return container.numLocalCandidates();
	}

	public void removeTheContainerFromCandidates(ParsedURL containerPURL)
	{
		C candidate = getInfoCollector().getContainer(container, null, null, containerPURL, false,
				false, false);
		getInfoCollector().removeCandidateContainer(candidate);
	}

	/**
	 * add an image+text surrogate for this that was extracted from a different document. FIXME this
	 * currently does the same thing as a surrogate extracted from this, but we might want to make a
	 * special collection for these "anchor surrogates".
	 */
	public void newAnchorImgTxt(ImgElement imgNode, ParsedURL anchorHref)
	{
		newImgTxt(imgNode, anchorHref);
	}

	/**
	 * For each anchorContext: create purl and check to see Aggregates AnchorContext by their
	 * destination hrefs. sets the metadata creates a container adds an outlink from the ancestor
	 * 
	 */
	public void generateCandidateContainersFromContexts(ArrayList<AnchorContext> anchorContexts,
			boolean fromContentBody)
	{
		HashMapArrayList<ParsedURL, ArrayList<AnchorContext>> hashedAnchorContexts = new HashMapArrayList<ParsedURL, ArrayList<AnchorContext>>();
		for (AnchorContext anchorContext : anchorContexts)
		{

			ParsedURL destHref = anchorContext.getHref();
			if (destHref.isImg())
			{ // The href associated is actually an image. Create a new img element and associate text to
				// it.
				Image newImage					= infoCollector.getOrConstructImage(destHref);
				Document sourceDocument	= getDocument();
				newImage.constructClipping(sourceDocument, null, null, anchorContext.getAnchorText());
				continue;
			}

			ArrayList<AnchorContext> arrayList = hashedAnchorContexts.get(destHref);
			if (arrayList == null)
			{
				arrayList = new ArrayList<AnchorContext>();
				hashedAnchorContexts.put(destHref, arrayList);
			}
			arrayList.add(anchorContext);
		}
		// Now that we have aggregated AnchorContext,
		// We generate One SemanticAnchor per purl, that aggregates all the semantics of the set of
		// anchorContexts
		for (ParsedURL hrefPurl : hashedAnchorContexts.keySet())
		{
			ArrayList<AnchorContext> anchorContextsPerHref = hashedAnchorContexts.get(hrefPurl);

			SemanticAnchor semanticAnchor = new SemanticAnchor(fromContentBody ? LinkType.WILD_CONTENT_BODY : LinkType.WILD, hrefPurl, anchorContextsPerHref, purl(),
					1);
			// generateCanidateContainerFromContext(aggregated,container, false);
			createContainerFromSemanticAnchor(container, hrefPurl, semanticAnchor);
		}
	}

	protected void createContainerFromSemanticAnchor(C container, ParsedURL hrefPurl,
			SemanticAnchor semanticAnchor)
	{
		if (hrefPurl != null && !hrefPurl.isNull())
		{
			newAHref(hrefPurl); // Parser count maintenance.

			if (!infoCollector.accept(hrefPurl))
				return;

			MetaMetadataRepository mmdRepository = infoCollector.metaMetaDataRepository();
			MetaMetadata metaMetadata = mmdRepository.getDocumentMM(hrefPurl);
			OldContainerI hrefContainer = infoCollector.getContainer(container, null, metaMetadata, hrefPurl,
					false, false, false);

			if (hrefContainer == null || hrefContainer.recycled())
			{
				debug(" hrefContainer is null or recycled: " + hrefContainer);
				return; // Should actually raise an exception, but this could happen when a container is not
						// meant to be reincarnated
			}
				
			
			SemanticInLinks semanticInLinks = hrefContainer.semanticInLinks();
			if(semanticInLinks != null && !semanticInLinks.contains(semanticAnchor.sourcePurl()))
				hrefContainer.addSemanticInLink(semanticAnchor, container);
			else
				System.out.println("--- Ignoring cyclicly adding inlink to: " + hrefContainer + " from container.");
			if(!hrefContainer.isDownloadDone())
			{
				container.addCandidateOutlink(hrefContainer);
			}
			else
				System.out.println("Download is already done on " + hrefContainer + " . Not adding as candidate container for: " + container);
			
		}
	}

	static final PrefBoolean	SHOW_PAGE_STRUCTURE_PREF	= PrefBoolean.usePrefBoolean("show_page_structure", false);

	public void setRecognizedDocumentStructure(Class<? extends RecognizedDocumentStructure> pageType)
	{
		if (SHOW_PAGE_STRUCTURE_PREF.value())
		{
			Document metadata = getDocument();
			if (metadata != null)
				metadata.setPageStructure(pageType.getSimpleName());
			else
				error("Can't setPageStructure() cause NULL Metadata :-(");
		}
	}

	public static int	MAX_TEXT_CONTEXT_LENGTH	= 1500;

	/**
	 * trim the text context under the limit if it is too long.
	 * 
	 * @param textContext
	 * @return
	 */
	// FIXME -- call site for this should really be when we build contexts in walkAndTagDOM()
	public static String trimTooLongContext(String textContext)
	{
		if (textContext.length() > MAX_TEXT_CONTEXT_LENGTH)
			return textContext.substring(0, MAX_TEXT_CONTEXT_LENGTH);
		else
			return textContext;
	}

}