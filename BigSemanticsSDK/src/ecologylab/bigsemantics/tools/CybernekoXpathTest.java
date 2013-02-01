package ecologylab.bigsemantics.tools;

import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
import ecologylab.net.ParsedURL;

public class CybernekoXpathTest
{

	private static final String			SLASHDOT										= "http://slashdot.org/index2.pl?fhfilter=japan+earthquake";
	private static final String			SLASHDOT_XPATH							= "//div[@id='firehoselist']";
	private static final String			SLASHDOT_CHILD_XPATH				= ".";

	private static final String			FLICKR											= "http://www.flickr.com/photos/81124164@N00/4085549266/";
	private static final String			FLICKR_XPATH								= "//html/head/link[2]/@href";
	private static final String			FLICKR_CHILD_XPATH					= ".";

	private static final String			CARTOONS_AC_UK							= "http://www.cartoons.ac.uk/record/28011";
	private static final String			CARTOONS_AC_UK_XPATH				= "//*[@id='detailPublish']";
	// private static final String CARTOONS_AC_UK_XPATH = "//div[@id='detailPublish']"; //Should be same as above
	// private static final String CARTOONS_AC_UK_XPATH = "//*[@id='detailPublish']/h4/a[1]"; //Should pick the first link
	// private static final String CARTOONS_AC_UK_XPATH = "//*[@id='detailPublish']//a[1]"; //Should pick the first link
	private static final String			CARTOONS_AC_UK_CHILD_XPATH	= ".";

	private static final String			WIKIPEDIA										= "http://en.wikipedia.org/wiki/Modern_art";
	private static final String			WIKIPEDIA_XPATH							= "//div[@id='bodyContent']/p[1]";
	private static final String			WIKIPEDIA_CHILD_XPATH				= ".";

	private static final String			LOCATION										= SLASHDOT;
	private static final String			XPATH												= SLASHDOT_XPATH;
	private static final String			CHILD_XPATH									= SLASHDOT_CHILD_XPATH;

	private static final ParsedURL	PURL												= ParsedURL.getAbsolute(LOCATION);

	private static boolean					useUpper										= false;

	public static void main(String[] args)
	{
		CybernekoWrapper cyberneko = new CybernekoWrapper();
		XPath xpath = XPathFactory.newInstance().newXPath();

		try
		{
			InputStream inStream = PURL.connect().inputStream();
			Document contextNode = cyberneko.parseDOM(inStream, System.out);
			
			String parentXPathString;
			if (useUpper)
				parentXPathString = cyberneko.xPathTagNamesToLower(XPATH);
			else
				parentXPathString = XPATH;

			NodeList parentNodeList = (NodeList) xpath.evaluate(parentXPathString, contextNode, XPathConstants.NODESET);
			String childXPath;
			if (useUpper)
				childXPath = cyberneko.xPathTagNamesToLower(CHILD_XPATH);
			else
				childXPath = CHILD_XPATH;
			System.out.println("\n\nList Size: " + parentNodeList.getLength());
			for (int i = 0; i < parentNodeList.getLength(); i++)
			{
				Node node = parentNodeList.item(i);
				System.out.println(node);
				String pNode = xpath.evaluate(childXPath, node);
				System.out.println("Result " + i + " =\t" + pNode.replaceAll("\\s+", " "));
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String getAllTextFromNode(Node node)
	{
		StringBuilder buffy = StringBuilderUtils.acquire();
		getAllTextFromNode(node, buffy);
		String result = buffy.toString();
		StringBuilderUtils.release(buffy);
		return result;
	}

	/**
	 * This method get all the text from the subtree rooted at a node.The reason for this
	 * implementation is that when we write a xpath we might get node of any type. Now if the node has
	 * some text inside it we would like to get it. And so this method get all the text in the subtree
	 * rooted at that node. Eliminates all formatting tags.
	 * 
	 * @param node
	 * @return
	 */
	private static void getAllTextFromNode(Node node, StringBuilder buffy)
	{
		short nodeType = node.getNodeType();
		switch (nodeType)
		{
		case Node.TEXT_NODE:
		case Node.CDATA_SECTION_NODE:
			buffy.append(node.getNodeValue());
			break;
		case Node.ATTRIBUTE_NODE:
		case Node.COMMENT_NODE:
		case Node.PROCESSING_INSTRUCTION_NODE:
			break;
		default:
			NodeList cList = node.getChildNodes();
			if (cList != null)
			{
				for (int k = 0; k < cList.getLength(); k++)
				{
					buffy.append(getAllTextFromNode(cList.item(k)));
				}
			}
			break;
		}
	}

}
