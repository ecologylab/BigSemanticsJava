package ecologylab.semantics.documentparsers;

import java.util.HashMap;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.model.TextChunkBase;
import ecologylab.semantics.model.text.utils.Filter;
import ecologylab.semantics.seeding.SemanticsPrefs;



public abstract class HTMLParserCommon
extends ContainerParser implements SemanticsPrefs
{

	private	boolean 	isFile = false;

	protected boolean 	bold;
	protected boolean 	italic;

	protected TextChunkBase	textChunk = null;

	protected boolean 		addingLinkWords;

	/** <code>Filter</code> that recognizes junk images from URL */
	public static final Filter 		filter			= new Filter();	// url filtering

	public HTMLParserCommon(NewInfoCollector infoCollector)
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
		int mimeIndex = parsedURL.mediaMimeIndex();

		switch( ImageFeatures.designRole(width, height, mimeIndex, isMap))
		{
		case ImageFeatures.INFORMATIVE:
		case ImageFeatures.UNKNOWN:
			Image image	= new Image(parsedURL);
			image.setWidth(width);
			image.setHeight(height);
			Document sourceDocument = getDocument();
			image.constructClippingCandidate(sourceDocument, outlink, alt, context);
			break;
		case ImageFeatures.UN_INFORMATIVE:
		default:
			infoCollector.registerUninformativeImage(parsedURL);
		}
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
		Document document	= getDocument();
		return (parsedURL != null && 
				infoCollector.accept(parsedURL) && 
				(!parsedURL.getName().startsWith("File:") && 
					parsedURL.crawlable() && !document.isJustCrawl() &&
					(document == null || parsedURL.isImg() || 
					(isFile && parsedURL.isHTML()) || !isFile)));
	}

	protected ParsedURL buildPurl(String urlString)
	{
		Document sourceDocument	= getDocument();
		return sourceDocument.isAnonymous() ? 
				ParsedURL.createFromHTML(null, urlString, false) :
			  sourceDocument.getLocation().createFromHTML(urlString, isSearchPage());
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
				Document hrefDocument	= infoCollector.getOrConstructDocument(hrefPurl);
				getDocument().addCandidateOutlink(hrefDocument);
			}
		}
	}

	/**
	 * create image and text surrogates for this HTML document, and add these surrogates into the
	 * localCollection in Container.
	 */
	public Image newImgTxt(ImgElement imgNode, ParsedURL anchorHref)
	{
		String alt	= imgNode.getAlt();
		int width		= imgNode.getWidth();
		int height	= imgNode.getHeight();
		boolean isMap 		= imgNode.isMap();

		ParsedURL srcPurl = imgNode.getSrc();

		if (anchorHref != null)
			processHref(anchorHref);

		Image result			= null;
		if (srcPurl != null)
		{
			String textContext = imgNode.getTextContext();

			if (alt != null)
				alt = alt.trim();
			
			result						= infoCollector.getOrConstructImage(srcPurl);
			Document outlink	= infoCollector.getOrConstructDocument(anchorHref);
			result.constructClippingCandidate(getDocument(), outlink, imgNode.getAlt(), imgNode.getTextContext());
		}

		return result;
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
		if (addingLinkWords && (textChunk != null))
			textChunk.endLink();
		addingLinkWords	= false;
	}

}