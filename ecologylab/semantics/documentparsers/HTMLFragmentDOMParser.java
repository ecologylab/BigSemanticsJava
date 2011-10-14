package ecologylab.semantics.documentparsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.html.DOMParserInterface;
import ecologylab.semantics.html.DOMWalkInformationTagger;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.metadata.builtins.AnonymousDocument;
import ecologylab.serialization.XMLTools;

public class HTMLFragmentDOMParser extends HTMLDOMParser implements DOMParserInterface
{

	public static final String	HTML_TAG_BODY	= "body";

	public static final String	HTML_TAG_IMG	= "img";

	InputStream						fragmentStream;

	ArrayList<ImgElement>	imageElements	= new ArrayList<ImgElement>();

	ParsedURL							containerPurl;

	public ParsedURL getContainerPurl()
	{
		return containerPurl;
	}

	StringBuilder	bodyTextBuffy	= new StringBuilder();

	public HTMLFragmentDOMParser(SemanticsSessionScope infoCollector, InputStream inputStream)
	{
		super(infoCollector);
		fragmentStream = inputStream;
		AnonymousDocument anonymousDocument = new AnonymousDocument();
		this.documentClosure = anonymousDocument.getOrConstructClosure();
	}

	
	
	/*
	This is the old parse method as of oct 13, 2011.
	This will be removed as soon as I am happy tweaking the old one.
	-Rhema
	
	@Override
	public void parse() throws IOException
	{
		org.w3c.dom.Document doc = getDom();
		int containerNodeIndex = 0;
		NodeList bodyNodeList = doc.getElementsByTagName(HTML_TAG_BODY);
		//types not passed well... check here
		if (bodyNodeList.getLength() > 0)
		{
			Node bodyNode = bodyNodeList.item(0);

			NodeList children = bodyNode.getChildNodes();
			if (children.getLength() > 0)
			{
				for (int i = 0; i < children.getLength(); i++)
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
					containerNode = getContainerAttrNode(children.item(containerNodeIndex));
				}
				setContainerPurl(containerNode);
			}
			DOMWalkInformationTagger.getTextInSubTree2(bodyNode, true, bodyTextBuffy, false, true);
		}
		NodeList imgNodeList = doc.getElementsByTagName(HTML_TAG_IMG);
		int numImages = imgNodeList.getLength();
		if (numImages > 0)
		{
			for (int i = 0; i < numImages; i++)
			{
				Node imgNode = imgNodeList.item(i);
				if (containerPurl == null)
				{
					setContainerPurl(getContainerAttrNode(imgNode));
				}

				// Add other information if available
				String altText = null;
				
				for (int k = 0; k < imgNode.getAttributes().getLength(); k++)
				{
					Node att = imgNode.getAttributes().item(k);
					// debug(att.getNodeName());
					if (att.getNodeName().toLowerCase().equals("alt"))
					{
						altText = att.getNodeValue();
					}
				}

				ImgElement im = new ImgElement(imgNode, containerPurl);
				im.setAlt(altText);
				imageElements.add(im);
			}

		}
		// taggedDoc.generateCollectionsFromRoot(rootNode);
	} 

*
*
*/
	
	private static HashMap<String, Integer> namesOfBreaklineNodeNames = null;
	
	/**
	 * @author rhema
	 *  returns true when a breakline would make sense based on the node name
	 * 
	 * @param nodeName such as p, div, br
	 * @return
	 */
	private boolean shouldBreakLineWithNodeName(String nodeName)
	{
		String name = nodeName.toLowerCase();
		if(namesOfBreaklineNodeNames == null)
		{
			namesOfBreaklineNodeNames = new HashMap<String,Integer>();
			namesOfBreaklineNodeNames.put("p",1);
			namesOfBreaklineNodeNames.put("h1",1);
			namesOfBreaklineNodeNames.put("h2",1);
			namesOfBreaklineNodeNames.put("h3",1);
			namesOfBreaklineNodeNames.put("h4",1);
			namesOfBreaklineNodeNames.put("h5",1);
			namesOfBreaklineNodeNames.put("h6",1);
			namesOfBreaklineNodeNames.put("br",1);
		}
		return namesOfBreaklineNodeNames.containsKey(name);
	}
	
	public String walkDomAddingTextAndAddNewlines(Node bodyNode)
	{
	  String s ="";
		NodeList children = bodyNode.getChildNodes();
		boolean addLine = false; //this is outside of the loop below to make it work correctly
	  for (int i = 0; i < children.getLength(); i++)
	  {	
	  	Node kid = children.item(i);
		//debug("Looooopy loop lop lop..."+kid.getNodeName());
		if(addLine == false)
		    addLine = shouldBreakLineWithNodeName(kid.getNodeName());
		if(kid.getNodeValue() != null)
		{
			String v = kid.getNodeValue();
			s = addWithOneSpaceBetween(s, v,false);
			if(addLine)
			{
				s+= "\n";
				addLine = false;
			}
		}
		  s = addWithOneSpaceBetween(s,walkDomAddingTextAndAddNewlines(kid),true);
	  }
	 
	  return s;
	}



	private String addWithOneSpaceBetween(String s, String v, boolean newlineOK)
	{
		if(!newlineOK)
		   v = v.replaceAll("\\n","");
		v = v.replaceAll("^[\\s]+", "");
		if(v.length()>0)
		{
			if(s.length()>0)
				if(s.charAt(s.length() -1) != ' ')
				{
					s+= " ";
				}
		   s+=v;
		}
		return s;
	}
	
	void checkForContainerAndSetPURL(Node bodyNode)
	{
		debug("looking at"+bodyNode.getNodeName());
		bodyNode.getAttributes();
	  if(bodyNode.getAttributes() != null)// bodyNode. .getNamedItem("container") != null)
	  {
	  	if(bodyNode.getAttributes().getNamedItem("container") != null)
	  	{
			  String containerValue = bodyNode.getAttributes().getNamedItem("container").getTextContent();
	     if (containerValue != null && containerValue.length() > 0)
			  {
				  containerValue = XMLTools.unescapeXML(containerValue);
				  containerPurl = ParsedURL.getAbsolute(containerValue);
				//  debug("FOUND THER PURLLLL!!!"+containerValue);
			    return;
			  }
	  	}
	  }
		NodeList children = bodyNode.getChildNodes();
	  for (int i = 0; i < children.getLength(); i++)
		{	
		  checkForContainerAndSetPURL(children.item(i));
		}
	}
	
	@Override
	public void parse() throws IOException
	{
		org.w3c.dom.Document doc = getDom();
		int containerNodeIndex = 0;
		NodeList bodyNodeList = doc.getElementsByTagName(HTML_TAG_BODY);
		if (bodyNodeList.getLength() > 0)
		{
			Node bodyNode = bodyNodeList.item(0);
			bodyTextBuffy.append( walkDomAddingTextAndAddNewlines(bodyNode));
			checkForContainerAndSetPURL(bodyNode);
		}
		
		NodeList imgNodeList = doc.getElementsByTagName(HTML_TAG_IMG);
		int numImages = imgNodeList.getLength();
		if (numImages > 0)
		{
			for (int i = 0; i < numImages; i++)
			{
				Node imgNode = imgNodeList.item(i);
				
				if (containerPurl == null)
				{
					setContainerPurl(getContainerAttrNode(imgNode));
				}
				// Add other information if available
				String altText = null;
				for (int k = 0; k < imgNode.getAttributes().getLength(); k++)
				{
					Node att = imgNode.getAttributes().item(k);
					// debug(att.getNodeName());
					if (att.getNodeName().toLowerCase().equals("alt"))
					{
						altText = att.getNodeValue();
					}
				}
				ImgElement im = new ImgElement(imgNode, containerPurl);
				im.setAlt(altText);
				imageElements.add(im);
			}
		}
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
				containerValue = XMLTools.unescapeXML(containerValue);
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
