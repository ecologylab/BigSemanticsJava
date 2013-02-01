package ecologylab.bigsemantics.html;

import java.util.ArrayList;

import org.w3c.dom.Node;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.XMLTools;

public class DOMFragmentInformationTagger extends DOMWalkInformationTagger
{
	
	ArrayList<ImgElement> dndImages = new ArrayList<ImgElement>();
	StringBuilder dndText = new StringBuilder();
	ParsedURL containerPurl = null;

	public DOMFragmentInformationTagger(ParsedURL purl, DOMParserInterface tidyInterface)
	{
		super(purl, tidyInterface);
	}
	
	@Override
	public void printTag(Node node)
	{
		String tagName = node.getNodeName();
		if (containerPurl == null)
		{
			Node container = node.getAttributes().getNamedItem("container");
			if (container != null)
			{
				String containerValue = container.getNodeValue();
				if (containerValue != null && containerValue.length() > 0)
				{
					containerValue= XMLTools.unescapeXML(containerValue);
					containerPurl = ParsedURL.getAbsolute(containerValue);
				}
			}
		}
		if( "img".equals(tagName) )
		{   
			ImgElement imgElement = new ImgElement(node, purl);
			
			dndImages.add(imgElement);
		}
		else if ("body".equals(tagName))
		{
			getTextInSubTree(node, true, dndText, true, true);
		}
		
		// We need to delete a link to the file write part at the end -- EUNYEE
//		super.printTag(lexer, fout, mode, indent, node);
	}
	
	public ArrayList<ImgElement> getDNDImages()
	{
		return dndImages; 
	}

	public String getDNDText()
	{
		return dndText.toString();
	}

	public ParsedURL getContainerPurl()
	{
		return containerPurl;
	}
	
}
