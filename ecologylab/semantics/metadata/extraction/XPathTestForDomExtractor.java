package ecologylab.semantics.metadata.extraction;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;

public class XPathTestForDomExtractor
{
	private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.7) Gecko/20070914 Firefox/2.0.0.7";
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			Tidy tidy 									= new Tidy();
			tidy.setQuiet(true);
			tidy.setShowWarnings(false);
			XPath xpath 								= XPathFactory.newInstance().newXPath();
			ParsedURL purl = new ParsedURL(new URL("http://portal.acm.org/citation.cfm?id=288808.288812&coll=Portal&dl=GUIDE&CFID=15014692&CFTOKEN=37615628"));
			PURLConnection purlConnection 	= purl.connect(DEFAULT_USER_AGENT);
			Document tidyDOM 				= tidy.parseDOM(purlConnection.inputStream(),null);
			
			//for scalar values
			String evaluation = xpath.evaluate("//a[@name='FullText']/@href", tidyDOM);
			System.out.println(evaluation);
		
			//for collection <list> values			
			
			NodeList nodes = (NodeList) xpath.evaluate(".//div[@class='authors']//a/child::text()", tidyDOM, XPathConstants.NODESET);
			System.out.println(nodes.getLength());
			System.out.println(nodes.item(0).getNodeValue());
			
		}
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (XPathExpressionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
