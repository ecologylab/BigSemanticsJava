package ecologylab.semantics.documentparsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.generic.DomTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.html.DOMParserInterface;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.utils.HTMLNames;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.builtins.AnonymousDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.ImageClipping;
import ecologylab.serialization.XMLTools;

public class HTMLFragmentDOMParser extends HTMLDOMParser implements DOMParserInterface, HTMLNames
{
	InputStream									fragmentStream;
	
	Reader											reader;
	
	ArrayList<ImageClipping>		imageClippings	= new ArrayList<ImageClipping>();

	ParsedURL										containerPurl;
	
	Document										containerDocument;
	
	Document										textOutlink;

	StringBuilder	bodyTextBuffy	= new StringBuilder();

	private static HashMap<String, Integer>	namesOfBreaklineNodeNames	= null;


	public HTMLFragmentDOMParser(SemanticsGlobalScope infoCollector, Reader reader, InputStream inputStream)
	{
		super(infoCollector);
		fragmentStream 			= inputStream;
		this.reader					= reader;
		AnonymousDocument anonymousDocument = new AnonymousDocument();
		this.documentClosure= anonymousDocument.getOrConstructClosure();
	}

	@Override
	public void parse() throws IOException
	{
		org.w3c.dom.Document dom = getDom();
		//DomTools.prettyPrint(dom);
		
		int containerNodeIndex = 0;
		NodeList bodyNodeList = dom.getElementsByTagName(BODY);
		if (bodyNodeList.getLength() > 0)
		{
			Node bodyNode = bodyNodeList.item(0);
			parseText(bodyTextBuffy, bodyNode);
			
			checkForSimplSourceLocation(bodyNode);
			checkForMetadata(bodyNode);
		}

		parseImages(dom);
	}

	private void parseImages(org.w3c.dom.Document dom)
	{
		NodeList imgNodeList	= dom.getElementsByTagName(IMG);
		int numImages 				= imgNodeList.getLength();
		if (numImages > 0)
		{
			for (int i = 0; i < numImages; i++)
			{
				Node imgNode = imgNodeList.item(i);

				String src 				= DomTools.getAttribute(imgNode, SRC);
				src               = changeImageUrlIfNeeded(src);
				ParsedURL imgPurl	= ImgElement.constructPurl(containerPurl, src);
				if (imgPurl == null)
				{
					continue;
				}

				Document outlink	= null;
				Node parent 			= imgNode.getParentNode();
				boolean changeSourceDoc = false;
				do
				{
					if (A.equals(parent.getNodeName()))
					{
						String hrefString	= DomTools.getAttribute(parent, HREF);
						if (hrefString != null)
						{
							try
							{
							  StringBuilder newImgHrefBuf = StringBuilderUtils.acquire();
								changeSourceDoc = changeImageRefUrlAndSourceDocIfNeeded(hrefString, newImgHrefBuf);
								hrefString = newImgHrefBuf.length() > 0 ? newImgHrefBuf.toString() : hrefString;
								StringBuilderUtils.release(newImgHrefBuf);
							}
							catch (UnsupportedEncodingException e)
							{
								error("Image ref URL cannot be decoded because it is using unsupported encoding. " +
										  "We support UTF-8 only.");
								e.printStackTrace();
							}
							ParsedURL aHref						= ImgElement.constructPurl(containerPurl, hrefString);
							if (aHref != null)
								outlink									= getSemanticsScope().getOrConstructDocument(aHref);
						}
						break;
					}
					parent	= parent.getParentNode();		
				} while (parent != null);
				SemanticsGlobalScope semanticsSessionScope	= getSemanticsScope();
				Image image																	= semanticsSessionScope.getOrConstructImage(imgPurl);
				if (image != null)
				{
					String altText = DomTools.getAttribute(imgNode, ALT);
					ImageClipping imageClipping = null;
					if (changeSourceDoc)
					{
					  outlink.queueDownload();
				    imageClipping = image.constructClipping(outlink, null, altText, null);
					}
					else
					{
				    imageClipping = image.constructClipping(containerDocument, outlink, altText, null);
					}
					imageClippings.add(imageClipping);
				}				
			}
		}
	}
	
	/**
	 * For Google Image, we need to change the source of the image since we don't support HTTPS.
	 * 
	 * @param imgSrcAttr
	 *          The HTTPS image source.
	 * @return The HTTP image source that points to the same image.
	 */
	protected String changeImageUrlIfNeeded(String imgSrcAttr)
	{
		if (imgSrcAttr != null)
		{
	    if (Pattern.matches("https://encrypted-tbn\\d+.google.com/images?.*", imgSrcAttr)
	        || Pattern.matches("https://encrypted-tbn\\d+.gstatic.com/images?.*", imgSrcAttr))
			{
				imgSrcAttr = imgSrcAttr.replace("https://", "http://");
				imgSrcAttr = imgSrcAttr.replace("//encrypted-tbn", "//tbn");
				imgSrcAttr = imgSrcAttr.replace("gstatic.com", "google.com");
			}
		}
		return imgSrcAttr;
	}
	
  /**
   * Image ref URL is the URL of the referring page where the image appears in. In some cases, like
   * Google Image, this ref URL is encoded as a URL parameter, and we need to extract it.
   * 
   * @param imgHref
   *          The original image ref URL.
   * @param outNewImgHref
   *          Buffer to hold the real image ref URL. By default it is the same as the imgHref, but
   *          in cases needed it will be different.
   * @return If we should change the image's source_doc to outNewImgHref.
   * @throws UnsupportedEncodingException
   */
	protected boolean changeImageRefUrlAndSourceDocIfNeeded(String imgHref,
	                                                        StringBuilder outNewImgHref)
			throws UnsupportedEncodingException
	{
		if (imgHref != null)
		{
			if (imgHref.startsWith("http://www.google.com/imgres?"))
			{
				ParsedURL hrefPURL = ParsedURL.getAbsolute(imgHref);
				Map<String, String> params = hrefPURL.extractParams(false);
				if (params != null)
				{
					if (params.containsKey("imgrefurl"))
					{
						String newImgHref = params.get("imgrefurl");
						newImgHref = URLDecoder.decode(newImgHref, "utf-8");
						if (outNewImgHref != null)
						  outNewImgHref.append(newImgHref);
						return true;
					}
				}
			}
			else
			{
			  if (outNewImgHref != null)
			    outNewImgHref.append(imgHref);
			}
		}
		return false;
	}

	public void parseText(StringBuilder buffy, Node bodyNode)
	{
		//debug("Node:" + bodyNode.getNodeName() + ":" + bodyNode.getNodeValue());
		
		NodeList children = bodyNode.getChildNodes();
		boolean addLine = false; // this is outside of the loop below to make it work correctly
		for (int i = 0; i < children.getLength(); i++)
		{
			Node kid = children.item(i);
			
			if (A.equals(kid.getNodeName()) && textOutlink == null)	// first cut; needs refinement
			{
				String hrefString	= DomTools.getAttribute(kid, HREF);
				if (hrefString != null)
				{
					ParsedURL aHref						= ImgElement.constructPurl(containerPurl, hrefString);
					if (aHref != null)
						textOutlink							= getSemanticsScope().getOrConstructDocument(aHref);
				}
			}

			if (addLine == false)
				addLine = shouldBreakLineWithNodeName(kid.getNodeName());
			if (kid.getNodeValue() != null)
			{
				String v = kid.getNodeValue();
				if (kid.getNodeName().equals(HASH_COMMENT))
					continue;
				addWithOneSpaceBetween(buffy, v, false);
				if (addLine)
				{
					buffy.append('\n');
					addLine = false;
				}
			}
			else if(shouldBreakLineWithNodeName(kid.getNodeName()))
			{
				buffy.append('\n');
			}
			//addWithOneSpaceBetween(buffy, walkDomAddingTextAndAddNewlines(kid), true);
			parseText(buffy, kid);
		}
	}

	private static void addWithOneSpaceBetween(StringBuilder buffy, String v, boolean newlineOK)
	{
		char lastChar	= (buffy.length() > 0) ? buffy.charAt(buffy.length() - 1) : ' ';
		if (lastChar != '\n')
			buffy.append(' ');
		
		if (!newlineOK )
			v = v.replaceAll("\\n", " ");
		v = v.replaceAll("^[\\s]+", "");
		v = v.replaceAll("[\\s]+", " ");
		if (v.length() > 0)
			buffy.append(v);
	}		
	
	/**
	 * @author rhema returns true when a breakline would make sense based on the node name.
	 * 
	 * @param nodeName
	 *          such as p, div, br
	 * @return
	 */
	private static boolean shouldBreakLineWithNodeName(String nodeName)
	{
		String name = nodeName.toLowerCase();
		if (namesOfBreaklineNodeNames == null)
		{
			namesOfBreaklineNodeNames = new HashMap<String, Integer>();
			namesOfBreaklineNodeNames.put("p", 1);
			namesOfBreaklineNodeNames.put("h1", 1);
			namesOfBreaklineNodeNames.put("h2", 1);
			namesOfBreaklineNodeNames.put("h3", 1);
			namesOfBreaklineNodeNames.put("h4", 1);
			namesOfBreaklineNodeNames.put("h5", 1);
			namesOfBreaklineNodeNames.put("h6", 1);
			namesOfBreaklineNodeNames.put("br", 1);
			namesOfBreaklineNodeNames.put("div", 1);
		}
		return namesOfBreaklineNodeNames.containsKey(name);
	}

	void checkForSimplSourceLocation(Node node)
	{
		node.getAttributes();
		if (node.getAttributes() != null && setContainerLocation(node) != null)
		{
			return;
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			checkForSimplSourceLocation(children.item(i));
		}
	}
	
	private ParsedURL setContainerLocation(Node elementNode)
	{
		if (containerPurl == null && elementNode != null)
		{
			String containerLocation = DomTools.getAttribute(elementNode, SIMPL_SOURCE_LOCATION);
			
			if (containerLocation == null || containerLocation.length() == 0)
				containerLocation = DomTools.getAttribute(elementNode, SIMPL);
			
			if (containerLocation == null || containerLocation.length() == 0)
				containerLocation = DomTools.getAttribute(elementNode, CONTAINER);

			if (containerLocation != null && containerLocation.length() > 0)
			{
				containerLocation 	= XMLTools.unescapeXML(containerLocation);
				containerPurl 		= ParsedURL.getAbsolute(containerLocation);
				containerDocument	= getSemanticsScope().getOrConstructDocument(containerPurl);
			}
		}
		return containerPurl;
	}
	
	void checkForMetadata(Node node)
	{
		node.getAttributes();
		if (node.getAttributes() != null && parseInjectedMetadata(node) != null)
		{
			return;
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			checkForMetadata(children.item(i));
		}
	}

	private ParsedURL parseInjectedMetadata(Node elementNode)
	{
		if (containerPurl == null && elementNode != null)
		{
			String containerMetadata = DomTools.getAttribute(elementNode, SIMPL_METADATA);
			DomTools.prettyPrint(elementNode);
			
			if (containerMetadata != null && containerMetadata.length() > 0)
			{
				System.out.println("\n\nsimpl:metadata:\n"+containerMetadata+"\n\n");
				Document metadataFromBrowser	= Document.constructAndMapFromJson(containerMetadata, getSemanticsScope());				
				if (metadataFromBrowser != null) 
				{
					// workflows need to be modified to accomodate metadata coming from drag
					System.out.println("\nSetting container document to injected metadata\n");
					
					containerDocument	= metadataFromBrowser;
					containerPurl 		= metadataFromBrowser.getLocation();
				}
			}
		}
		return containerPurl;
	}

	@Override
	public InputStream inputStream()
	{
		return fragmentStream;
	}

	@Override
	public Reader reader()
	{
		return reader;
	}
	public String getBodyText()
	{
		return bodyTextBuffy.toString();
	}
	public Document getTextOutlink()
	{
		return textOutlink;
	}

	public ArrayList<ImageClipping> getImageClippings()
	{

		return imageClippings;
	}

	public void setContent()
	{
	}

	public void setIndexPage()
	{
	}

	public Document getContainerDocument()
	{
		return containerDocument;
	}

	public ParsedURL getContainerPurl()
	{
		return containerPurl;
	}
	
	@Override
	public void recycle()
	{
		fragmentStream 		= null;
		reader 						= null;
		imageClippings.clear();
		imageClippings 		= null;
		containerPurl			= null;
		containerDocument	= null;
		textOutlink				= null;
		bodyTextBuffy			= null;
		super.recycle();
	}

}
