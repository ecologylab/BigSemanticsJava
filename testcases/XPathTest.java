package testcases;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.utils.StringBuilderUtils;

public class XPathTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Tidy 									tidy 			= new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		ParsedURL purl = ParsedURL.getAbsolute("http://portal.acm.org/citation.cfm?id=1460563.1460642&amp;coll=GUIDE&amp;dl=GUIDE&amp;CFID=48444641&amp;CFTOKEN=72936343");
		XPath xpath = XPathFactory.newInstance().newXPath();
		try
		{
			Document contextNode = tidy.parseDOM(new FileInputStream(new File("C:\\abhinavCode\\ecologylabSemantics\\testcases\\file2.xml")), System.out);
			String parentXPathString=".//a[@name='references']/../following-sibling::table//a[@href[starts-with(.,'citation')]]";
			
				DTMNodeList parentNodeList =(DTMNodeList) xpath.evaluate(parentXPathString, contextNode, XPathConstants.NODESET);
				String childXPath = "./child::text()";
				for(int i=0;i<parentNodeList.getLength();i++)
				{
					Node node = parentNodeList.item(i);
					//System.out.println(node);
					String pNode = xpath.evaluate(childXPath, node);
					System.out.println("Result "+i+" =\t"+pNode);
				}
		}
		catch (XPathExpressionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
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
