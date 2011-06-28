package ecologylab.semantics.metametadata.test;

import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.DOMNodeListImpl;
import org.w3c.tidy.Tidy;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.dom.IDOMProvider;
import ecologylab.semantics.html.utils.StringBuilderUtils;

public class XPathTest
{

	private static final String	ACM_CHILD_XPATH	= "./child::text()";
	private static final String	ACM_CITATION	= "http://portal.acm.org/citation.cfm?id=1460563.1460642&amp;coll=GUIDE&amp;dl=GUIDE&amp;CFID=48444641&amp;CFTOKEN=72936343";
	private static final String	CITATION_XPATH	= ".//a[@name='references']/../following-sibling::table//a[@href[starts-with(.,'citation')]]";

	private static final String	WIKIPEDIA	= "http://en.wikipedia.org/wiki/Harbor_Seal";
	private static final String	WIKIPEDIA_XPATH	= "//*[starts-with(@class,'infobox')]//img[1]";
	private static final String	WIKIPEDIA_CHILD_XPATH	= "./@height";
//	private static final String	XPATH	= CITATION_XPATH;
//	private static final String	LOCATION	= ACM_CITATION;
//	private static final String	XPATH	= CITATION_XPATH;
	
	private static final String	TRENDS	= "http://www.google.com/trends";
	private static final String	TRENDS_XPATH	= "//td[@class='hotListTable']";
	private static final String	TRENDS_CHILD_XPATH	= ".";
	
	private static final String	FLICKR	= "http://www.flickr.com/photos/81124164@N00/4085549266/";
	private static final String	FLICKR_XPATH	= "//html/head/link[2]/@href";
	private static final String	FLICKR_CHILD_XPATH	= ".";

	private static final String	IMDB	= "http://www.imdb.com/title/tt1464540/";
	///div[@id='filmo-head-Actor']/following-sibling::*
	private static final String	IMDB_XPATH	= "//div[@class='mediastrip']//img/@src";
	private static final String	IMDB_CHILD_XPATH	= ".";
	
	private static final String GOOGLE_BOOKS = "http://books.google.com/books?id=fu5HtixRje8C&dq=o%27reilly&source=gbs_navlinks_s";
	private static final String GOOGLE_BOOKS_XPATH = "//div[@id='citations_module_v']/div[2]//div";
	private static final String GOOGLE_BOOKS_CHILD_XPATH = "./div/a";
	
	private static final String	SLASHDOT	= "http://slashdot.org/index2.pl?fhfilter=japan+earthquake";
	private static final String	SLASHDOT_XPATH	= "//div[@id='firehoselist']//h2[@class='story'][1]/following-sibling::div[@class='grid_14'][1]//a[@class='popular tag']";
	private static final String	SLASHDOT_CHILD_XPATH	= ".";
	
	private static final String	LOCATION		= SLASHDOT;
	private static final String	XPATH				= SLASHDOT_XPATH;
	private static final String	CHILD_XPATH	= SLASHDOT_CHILD_XPATH;
	
	private static final ParsedURL PURL = ParsedURL.getAbsolute(LOCATION);


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Tidy tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		XPath xpath = XPathFactory.newInstance().newXPath();
		try
		{
//		InputStream inStream = new FileInputStream(new File("C:\\abhinavCode\\ecologylabSemantics\\testcases\\file2.xml"));
			InputStream inStream = PURL.connect().inputStream();
			Document contextNode = tidy.parseDOM(inStream, null);
			String parentXPathString=XPATH;
			
				DOMNodeListImpl parentNodeList = (DOMNodeListImpl) xpath.evaluate(parentXPathString, contextNode, XPathConstants.NODESET);
				String childXPath = CHILD_XPATH;
				System.out.println("List Size: " + parentNodeList.getLength());
				for(int i=0;i<parentNodeList.getLength();i++)
				{
					Node node = parentNodeList.item(i);
					System.out.println(node);
					String pNode = xpath.evaluate(childXPath, node);
					System.out.println("Result "+i+" =\t"+pNode);
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
