package ecologylab.semantics.documentparsers;

import java.util.ArrayList;

import org.w3c.tidy.DOMNodeImpl;
import org.w3c.tidy.TdNode;
import org.w3c.tidy.Tidy;

import ecologylab.appframework.types.prefs.PrefBoolean;
import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.TidyInterface;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.semantics.html.documentstructure.LinkType;
import ecologylab.semantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.semantics.html.documentstructure.SemanticAnchor;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.TextClipping;
import ecologylab.serialization.XMLTools;

/**
 * Parse HTML page and create DOM
 * 
 * @author eunyee
 * 
 */
public class HTMLDOMParser
		extends HTMLParserCommon
		implements TidyInterface
{

	/**
	 * Root DOM of the current document
	 */
	private org.w3c.dom.Document	dom;

	protected Tidy								tidy	= new Tidy();

	
	boolean indexPage = false;
	boolean contentPage = false;

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
	public void newTxt(ParagraphText paraText)
	{
		if ((paraText != null) && (paraText.length() > 0))
		{
			StringBuilder buffy	= paraText.getBuffy();
			if (buffy.indexOf("@") == -1 )	 // filter out paragraphs with email addresses
			{
				TextClipping textClipping	= new TextClipping(StringTools.toString(buffy), false);
				
				getDocument().addCandidateTextClipping(textClipping);
			}
		}
	}

	public int numCandidatesExtractedFrom()
	{
		return getDocument().sizeLocalCandidates();
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
				newImage.constructClipping(getDocument(), null, null, anchorContext.getAnchorText());
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

			SemanticAnchor semanticAnchor = 
				new SemanticAnchor(fromContentBody ? LinkType.WILD_CONTENT_BODY : LinkType.WILD, hrefPurl, anchorContextsPerHref, purl(), 1);

			handleSemanticAnchor(semanticAnchor, hrefPurl);
		}
	}

	protected void handleSemanticAnchor(SemanticAnchor semanticAnchor, ParsedURL hrefPurl)
	{
		if (hrefPurl != null && !hrefPurl.isNull() && infoCollector.accept(hrefPurl))
		{
			Document hrefDocument		= infoCollector.getOrConstructDocument(hrefPurl);
			if (hrefDocument == null || hrefDocument.isRecycled())
			{
				warning("hrefDocument is null or recycled: " + hrefPurl);
				return; // Should actually raise an exception, but this could happen when a container is not
						// meant to be reincarnated
			}
			Document sourceDocument	= getDocument();
			
			hrefDocument.addSemanticInlink(semanticAnchor, sourceDocument);
			sourceDocument.addCandidateOutlink(hrefDocument);			
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
	@Override
	public void setContent ( )
	{
		contentPage = true;		
	}

	@Override
	public void setIndexPage ( )
	{
		indexPage = true;
	}

	@Override
	public boolean isIndexPage ( )
	{
		return indexPage;
	}

	@Override
	public boolean isContentPage ( )
	{
		return contentPage;
	}

	@Override
	public void removeTheContainerFromCandidates(ParsedURL containerPURL)
	{
		warning("Not Implemented: removeTheContainerFromCandidates(" + containerPURL);
	}


}