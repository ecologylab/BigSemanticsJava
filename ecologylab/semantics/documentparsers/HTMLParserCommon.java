package ecologylab.semantics.documentparsers;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.ImageClipping;
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

	public HTMLParserCommon(SemanticsSessionScope infoCollector)
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
		//FIXME -- should we depend on Seeding here?? or do this in post-processing for CompoundDocumentParserCrawlerResult??
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
	 * add an image+text surrogate for this that was extracted from a different document. FIXME this
	 * currently does the same thing as a surrogate extracted from this, but we might want to make a
	 * special collection for these "anchor surrogates".
	 */
	public ImageClipping constructAnchorImageClipping(ImgElement imgNode, ParsedURL anchorHref)
	{
		CompoundDocument outlink	= (CompoundDocument) infoCollector.getOrConstructDocument(anchorHref);
		
		return constructImageClipping(getDocument(), outlink, null, imgNode);
	}
	/**
	 * create image and text surrogates for this HTML document, and add these surrogates into the
	 * localCollection in Container.
	 */
	public ImageClipping constructImageClipping(ImgElement imgNode, ParsedURL anchorHref)
	{
		Document outlink				= infoCollector.getOrConstructDocument(anchorHref);
		Document sourceDocument = getDocument();
		return constructImageClipping(sourceDocument, sourceDocument, outlink, imgNode);
	}
	/**
	 * Construct an ImageClipping, associating it properly in the hypermedia graph.
	 * 
	 * @param basisDocument		The CompoundDocument to add the clipping to. 
	 * @param sourceDocument	The CompoundDocument to be listed as the Clipping's source. The one it is a surrogate for.
	 * 												Usually the same as basisDocument, but for a surrogate for X, found in Y, instead uses outlink here.
	 * @param outlink					The Document to be listed as the Clipping's href destination.
	 * @param imgNode					Representation of the source HTML + textContext and additional extractedCaption.
	 * 
	 * @return
	 */
	public ImageClipping constructImageClipping(Document basisDocument, Document sourceDocument, Document outlink, ImgElement imgNode)
	{
		ParsedURL srcPurl = imgNode.getSrc();

		ImageClipping result			= null;
		if (srcPurl != null)
		{
			int width			= imgNode.getWidth();
			int height		= imgNode.getHeight();
			int mimeIndex	= srcPurl.mediaMimeIndex();
			boolean isMap = imgNode.isMap();

			switch (ImageFeatures.designRole(width, height, mimeIndex, isMap))
			{
			case ImageFeatures.INFORMATIVE:
			case ImageFeatures.UNKNOWN:
				String alt	= imgNode.getAlt();

				if (alt != null)
					alt = alt.trim();
				
				Image image						= infoCollector.getOrConstructImage(srcPurl);
				if (image == null)
					return null;
				image.setWidth(width);
				image.setHeight(height);
				
				result	= image.constructClipping(basisDocument, sourceDocument, outlink, alt, imgNode.getTextContext());
				result.setXpath(imgNode.xpath());
				break;
			case ImageFeatures.UN_INFORMATIVE:
			default:
				infoCollector.getGlobalCollection().registerUninformativeImage(srcPurl);
			}
		}
		return result;
	}
	
	public static boolean isAd ( ParsedURL hrefPurl )
	{
		String lc			= hrefPurl.lc();
		boolean filterMatch	= SemanticsPrefs.FILTER_OUT_ADS.value() && filter.matchLc(lc);
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