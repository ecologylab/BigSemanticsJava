package ecologylab.semantics.documentparsers;

import java.util.HashMap;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.model.TextChunkBase;
import ecologylab.semantics.model.text.utils.Filter;
import ecologylab.semantics.seeding.SemanticsPrefs;
import ecologylab.serialization.ElementState;



public abstract class HTMLParserCommon<C extends Container, IC extends InfoCollector<C>>
extends ContainerParser<C, IC, ElementState> implements SemanticsPrefs
{

	private	boolean 	isFile = false;
	/**
	 * context that begins with &lt;a href="...", and remains current until </a>
	 */
	protected ParsedURL currentAnchorHref;

	protected boolean 	bold;
	protected boolean 	italic;
	/**
	 * Number of links inside this page. Used to measure if it is a
	 * content page, or a link collection.
	 */
	protected int 			numLinks;

	protected int 			numTokensNoLink;

	protected int 			maxTokensNoLink;

	protected TextChunkBase	textChunk = null;

	protected boolean 		addingLinkWords;

	protected boolean		gotHttpColon;
	/** <code>Filter</code> that recognizes junk images from URL */
	public static final Filter 		filter			= new Filter();	// url filtering

	public HTMLParserCommon(IC infoCollector)
	{
		super(infoCollector);
	}

	/**
	 * Parser found a bold (or strong) tag or an end bold tag.
	 */
	public void setBold(boolean on) 
	{
		bold	= on;
	}

	/**
	 * Parser found an italic (or em) tag or an end italic tag.
	 */
	public void setItalic(boolean on) 
	{
		italic	= on;
	}

	/**
	 * If we are collecting media, then try creating an ImgElement,
	 * and adding it to collections.
	 **/
	protected void newImg(ParsedURL parsedURL, String alt, int width,
			int height, boolean isMap) 
	{
		gotHttpColon		= false;

		if (container != null)
			container.createImageElementAndAddToPools(parsedURL, alt, width, height, isMap, currentAnchorHref);
	}



	public void newAHref(HashMap<String, String> attributesMap) 
	{
		newAHref(attributesMap.get("href"));
	}

	/**
	 * Called by the Parser in response to the <code>href</code> attribute
	 * of the <code>a</code> html element, or when processing other similar
	 * elements, such <code>iframe</code>.
	 * Forms the href from the argument and our context.
	 * Performs statistics gathering housekeeping regarding tokens and links.
	 */
	public ParsedURL newAHref(String urlString) 
	{
		ParsedURL hrefPurl		= buildAndFilterPurl(urlString); // processNewHref(urlString);

		newAHref(hrefPurl);
		return hrefPurl;
	}

	protected void newAHref(ParsedURL hrefPurl)
	{
		gotHttpColon		= false;

		if (currentAnchorHref == null)
		{
			if (numTokensNoLink > maxTokensNoLink)
				maxTokensNoLink	= numTokensNoLink;
			numTokensNoLink	= 0;
		}
		// add for the container and href. 
		if (hrefPurl != null)
		{	
			currentAnchorHref		= hrefPurl;
			numLinks++;
			//This seems wrong. We generate containers in HTMLDomParser 
			//processHref(hrefPurl);
		}
	}

	/**
	 * Create a ParsedURL from a urlString, in the context of this.
	 * Used for processing the <code>href</code> attribute of the <code>a</code>
	 * html element.
	 * Check to make sure its crawlable (for example, that its not mailto:).
	 * <p/>
	 * Add to collections if appropriate.
	 */
	public ParsedURL processNewHref(String urlString) 
	{
		gotHttpColon		= false;

		ParsedURL result = buildAndFilterPurl(urlString);
		
		if (result != null)
		{
			processHref(result);	// create the href container
		}	
		return result;
	}

	protected ParsedURL buildAndFilterPurl(String urlString)
	{
		ParsedURL result	= buildPurl(urlString);
		return (result != null) && filterPurl(result) ? result : null;
	}
		
	/**
	 * Filters the parsedURL to check if 
	 * <li>infoCollector accepts
	 * <li>name does not start with File
	 * <li>is Crawlable
	 * <br>Do less checking if it's drag'n'drop (container==null)
	 * @param urlString
	 * @return
	 */
	protected boolean filterPurl(ParsedURL parsedURL)
	{
		return (parsedURL != null && 
				infoCollector.accept(parsedURL) && 
				(!parsedURL.getName().startsWith("File:") && 
					parsedURL.crawlable() && 
					(container == null || parsedURL.isImg() || 
					( container.crawlLinks()  && (isFile && parsedURL.isHTML()) || !isFile))));
	}

	protected ParsedURL buildPurl(String urlString)
	{
		return (container != null) ?
				container.purl().createFromHTML(urlString, isSearchPage()) :
					// remove hash but not remove args. 
					ParsedURL.createFromHTML(null, urlString, false);
	}

	/**
	 * Add to collections if its accept-able -- in our web space.
	 */
	protected void processHref(ParsedURL hrefPurl) 
	{
		if (!isAd(hrefPurl))
		{
			if (hrefPurl.isImg())
				newImg(hrefPurl, null, 0 , 0, false);
			else 
			{
				//FIXME -- abhinav -- how do we look up MetaMetadata here?!
				MetaMetadata metaMetadata	=infoCollector.metaMetaDataRepository().getDocumentMM(hrefPurl);

//				CfContainer newContainer = abstractInfoCollector.getContainerWithoutQueuing(container, hrefPurl);
//				if (newContainer != null)
//					container.addCandidateContainer(newContainer);
				infoCollector.getContainer(container, hrefPurl, false, true, null, metaMetadata, false);
			}
		}
	}

	private boolean isAd ( ParsedURL hrefPurl )
	{
		String lc			= hrefPurl.lc();
		boolean filterMatch	= !SemanticsPrefs.FILTER_OUT_ADS.value() && filter.match(lc);
		return filterMatch;
	}

	/**
	 * @return	true if <code>this</code> is a search page, and so needs
	 * special parsing of URLs, to unpack nested entries.
	 *		false in all other (usual) cases.
	 */
	public boolean isSearchPage() 
	{
		return false;
	}

	public void closeHref() 
	{
		gotHttpColon		= false;

		currentAnchorHref		= null;

		if (addingLinkWords && (textChunk != null))
			textChunk.endLink();
		addingLinkWords	= false;
	}

}