package ecologylab.semantics.html;

import java.util.ArrayList;

import org.w3c.tidy.AttVal;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Lexer;
import org.w3c.tidy.Out;
import org.w3c.tidy.TdNode;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.XMLTools;

public class DOMFragmentInformationTagger extends DOMWalkInformationTagger
{
	
	ArrayList<ImgElement> dndImages = new ArrayList<ImgElement>();
	StringBuilder dndText = new StringBuilder();
	ParsedURL containerPurl = null;

	public DOMFragmentInformationTagger(Configuration configuration, ParsedURL purl,
			TidyInterface tidyInterface)
	{
		super(configuration, purl, tidyInterface);
	}
	
	@Override
	protected void printTag(Lexer lexer, Out fout, short mode, int indent, TdNode node)
	{
		String tagName = node.element;
		if (containerPurl == null)
		{
			AttVal container = node.getAttrByName("container");
			if (container != null)
			{
				String containerValue = container.value;
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
			getTextInSubTree(node, true, dndText);
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
