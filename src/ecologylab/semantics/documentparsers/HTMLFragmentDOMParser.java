package ecologylab.semantics.documentparsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.generic.DomTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.html.DOMParserInterface;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.metadata.builtins.AnonymousDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metadata.builtins.ImageClipping;
import ecologylab.serialization.XMLTools;

public class HTMLFragmentDOMParser extends HTMLDOMParser implements DOMParserInterface
{

	public static final String	HTML_TAG_BODY	= "body";

	public static final String	HTML_TAG_IMG	= "img";

	InputStream									fragmentStream;
	
	Reader											reader;
	
	ArrayList<ImageClipping>		imageClippings	= new ArrayList<ImageClipping>();

	ParsedURL										containerPurl;
	
	Document										containerDocument;
	
	Document										textOutlink;

	StringBuilder	bodyTextBuffy	= new StringBuilder();

	public HTMLFragmentDOMParser(SemanticsSessionScope infoCollector, Reader reader, InputStream inputStream)
	{
		super(infoCollector);
		fragmentStream 	= inputStream;
		this.reader			= reader;
		AnonymousDocument anonymousDocument = new AnonymousDocument();
		this.documentClosure = anonymousDocument.getOrConstructClosure();
	}

	/*
	 * This is the old parse method as of oct 13, 2011. This will be removed as soon as I am happy
	 * tweaking the old one. -Rhema
	 * 
	 * @Override public void parse() throws IOException { org.w3c.dom.Document doc = getDom(); int
	 * containerNodeIndex = 0; NodeList bodyNodeList = doc.getElementsByTagName(HTML_TAG_BODY);
	 * //types not passed well... check here if (bodyNodeList.getLength() > 0) { Node bodyNode =
	 * bodyNodeList.item(0);
	 * 
	 * NodeList children = bodyNode.getChildNodes(); if (children.getLength() > 0) { for (int i = 0; i
	 * < children.getLength(); i++) { if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
	 * containerNodeIndex = i; break; } } Node containerNode; if (containerNodeIndex == 0) {
	 * containerNode = getContainerAttrNode(bodyNode); } else { containerNode =
	 * getContainerAttrNode(children.item(containerNodeIndex)); } setContainerPurl(containerNode); }
	 * DOMWalkInformationTagger.getTextInSubTree2(bodyNode, true, bodyTextBuffy, false, true); }
	 * NodeList imgNodeList = doc.getElementsByTagName(HTML_TAG_IMG); int numImages =
	 * imgNodeList.getLength(); if (numImages > 0) { for (int i = 0; i < numImages; i++) { Node
	 * imgNode = imgNodeList.item(i); if (containerPurl == null) {
	 * setContainerPurl(getContainerAttrNode(imgNode)); }
	 * 
	 * // Add other information if available String altText = null;
	 * 
	 * for (int k = 0; k < imgNode.getAttributes().getLength(); k++) { Node att =
	 * imgNode.getAttributes().item(k); // debug(att.getNodeName()); if
	 * (att.getNodeName().toLowerCase().equals("alt")) { altText = att.getNodeValue(); } }
	 * 
	 * ImgElement im = new ImgElement(imgNode, containerPurl); im.setAlt(altText);
	 * imageElements.add(im); }
	 * 
	 * } // taggedDoc.generateCollectionsFromRoot(rootNode); }
	 */

	private static HashMap<String, Integer>	namesOfBreaklineNodeNames	= null;

	/**
	 * @author rhema returns true when a breakline would make sense based on the node name.
	 * 
	 * @param nodeName
	 *          such as p, div, br
	 * @return
	 */
	private boolean shouldBreakLineWithNodeName(String nodeName)
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

	public void parseText(StringBuilder buffy, Node bodyNode)
	{
		//debug("Node:" + bodyNode.getNodeName() + ":" + bodyNode.getNodeValue());
		
		NodeList children = bodyNode.getChildNodes();
		boolean addLine = false; // this is outside of the loop below to make it work correctly
		for (int i = 0; i < children.getLength(); i++)
		{
			Node kid = children.item(i);
			
			if ("a".equals(kid.getNodeName()) && textOutlink == null)	// first cut; needs refinement
			{
				String hrefString	= DomTools.getAttribute(kid, "href");
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
				if (kid.getNodeName().equals("#comment"))
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
		{
//			if (s.length() > 0)
//				if (s.charAt(s.length() - 1) != ' ')
//				{
//					s += " ";
//				}
			buffy.append(v);
		}
	}

	void checkForSimplSourceLocation(Node node)
	{
		node.getAttributes();
		if (node.getAttributes() != null && setContainerDocument(node) != null)
		{
			return;
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			checkForSimplSourceLocation(children.item(i));
		}
	}

	@Override
	public void parse() throws IOException
	{
		org.w3c.dom.Document dom = getDom();
		//DomTools.prettyPrint(dom);
		
		int containerNodeIndex = 0;
		NodeList bodyNodeList = dom.getElementsByTagName(HTML_TAG_BODY);
		if (bodyNodeList.getLength() > 0)
		{
			Node bodyNode = bodyNodeList.item(0);
			parseText(bodyTextBuffy, bodyNode);
			checkForSimplSourceLocation(bodyNode);
		}

		NodeList imgNodeList = dom.getElementsByTagName(HTML_TAG_IMG);
		int numImages = imgNodeList.getLength();
		if (numImages > 0)
		{
			for (int i = 0; i < numImages; i++)
			{
				Node imgNode = imgNodeList.item(i);
			  //make sure src is set...
				String src 				= DomTools.getAttribute(imgNode, "src");
				ParsedURL imgPurl	= ImgElement.constructPurl(containerPurl, src);
				if (imgPurl == null)
				{
					System.out.println("Skipping img with no image source");
					continue;
				}
				String altText = DomTools.getAttribute(imgNode, "alt");

				Document outlink	= null;
				Node parent 		= imgNode.getParentNode();
				do
				{
					if ("a".equals(parent.getNodeName()))
					{
						String hrefString	= DomTools.getAttribute(parent, "href");
						if (hrefString != null)
						{
							ParsedURL aHref						= ImgElement.constructPurl(containerPurl, hrefString);
							if (aHref != null)
								outlink									= getSemanticsScope().getOrConstructDocument(aHref);
						}
						break;
					}
					parent	= parent.getParentNode();		
				} while (parent != null);
				SemanticsGlobalScope semanticsSessionScope	= getSemanticsScope();
				Image image		= semanticsSessionScope.getOrConstructImage(imgPurl);
				if (image != null)
				{
					ImageClipping imageClipping = image.constructClipping(containerDocument, outlink, altText, null);
					imageClippings.add(imageClipping);
				}				
			}
		}
	}

	private ParsedURL setContainerDocument(Node elementNode)
	{
		if (containerPurl == null && elementNode != null)
		{
			String containerValue = DomTools.getAttribute(elementNode, "simpl:source_location");
			if (containerValue == null || containerValue.length() == 0)
				containerValue = DomTools.getAttribute(elementNode, "simpl");
			if (containerValue == null || containerValue.length() == 0)
				containerValue = DomTools.getAttribute(elementNode, "container");

			if (containerValue != null && containerValue.length() > 0)
			{
				containerValue 		= XMLTools.unescapeXML(containerValue);
				containerPurl 		= ParsedURL.getAbsolute(containerValue);
				containerDocument	= getSemanticsScope().getOrConstructDocument(containerPurl);
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
