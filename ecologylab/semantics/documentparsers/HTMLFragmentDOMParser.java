package ecologylab.semantics.documentparsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.generic.DomTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.html.DOMFragmentInformationTagger;
import ecologylab.semantics.html.DOMParserInterface;
import ecologylab.semantics.html.DOMWalkInformationTagger;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.metadata.builtins.AnonymousDocument;
import ecologylab.serialization.XMLTools;

public class HTMLFragmentDOMParser
extends HTMLDOMParser
implements DOMParserInterface
{

	InputStream fragmentStream;
	
	ArrayList<ImgElement> imageElements	= new ArrayList<ImgElement>();
	
	ParsedURL							containerPurl;
	
	public ParsedURL getContainerPurl()
	{
		return containerPurl;
	}

	StringBuilder					bodyTextBuffy	= new StringBuilder();
	
	public HTMLFragmentDOMParser(SemanticsSessionScope infoCollector, InputStream inputStream)
	{
		super(infoCollector);
		fragmentStream 											= inputStream;
		AnonymousDocument anonymousDocument	= new AnonymousDocument();
		this.documentClosure								= anonymousDocument.getOrConstructClosure();
	}
	
	@Override
	public void parse() throws IOException
	{
		org.w3c.dom.Document doc	= getDom();
		DomTools.prettyPrint(doc);
		int containerNodeIndex = 0;
		NodeList bodyNodeList	= doc.getElementsByTagName("BODY");
		if (bodyNodeList.getLength() > 0)
		{
			Node bodyNode				= bodyNodeList.item(0);
			
			NodeList children = bodyNode.getChildNodes();
			if (children.getLength() > 0)
			{
				for (int i=0; i<children.getLength(); i++)
				{
					if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
					{
						containerNodeIndex = i;
						break;
					}
				}
				Node containerNode;
				if (containerNodeIndex == 0)
				{
					containerNode = getContainerAttrNode(bodyNode);
				}
				else
				{
					containerNode 			= getContainerAttrNode(children.item(containerNodeIndex));
				}
				setContainerPurl(containerNode);
			}
			DOMWalkInformationTagger.getTextInSubTree(bodyNode, true, bodyTextBuffy, true);
		}
		NodeList imgNodeList	= doc.getElementsByTagName("IMG");
		int numImages = imgNodeList.getLength();
		if (numImages > 0)
		{
			for (int i=0; i < numImages; i++)
			{
				Node imgNode = imgNodeList.item(i);
				if (containerPurl == null)
				{
					setContainerPurl(getContainerAttrNode(imgNode));
				}
				imageElements.add(new ImgElement(imgNode, containerPurl));
			}
		}
//		taggedDoc.generateCollectionsFromRoot(rootNode);
	}
	
	private Node getContainerAttrNode(Node elementNode)
	{
		return elementNode.getAttributes().getNamedItem("container");
	}

	private void setContainerPurl(Node containerNode)
	{
		if (containerNode != null)
		{
			String containerValue = containerNode.getNodeValue();
			if (containerValue != null && containerValue.length() > 0)
			{
				containerValue= XMLTools.unescapeXML(containerValue);
				containerPurl = ParsedURL.getAbsolute(containerValue);
			}
		}
	}
	
	public InputStream inputStream()
	{
		return fragmentStream;
	}
	
	public String getDNDText()
	{
		return bodyTextBuffy.toString();
	}
	
	public ArrayList<ImgElement> getDNDImages()
	{
		
		return imageElements;
	}

	public void setContent()
	{	
	}

	public void setIndexPage()
	{	
	}

}
