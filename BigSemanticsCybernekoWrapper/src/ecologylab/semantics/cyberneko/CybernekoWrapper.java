package ecologylab.semantics.cyberneko;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.cyberneko.html.filters.Writer;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import ecologylab.semantics.html.dom.IDOMProvider;

/**
 * Wraps the cyberneko DOM parser for use as a DOM provider
 * 
 * @author agh8154
 * 
 */
public class CybernekoWrapper implements IDOMProvider
{
	
	DOMParser parser;
	
	
	public CybernekoWrapper()
	{
		//XMLParserConfiguration parser = new HTMLConfiguration();
		parser = new DOMParser();
		try
		{
			parser.setFeature("http://xml.org/sax/features/namespaces", false);			parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
		}
		catch (SAXNotRecognizedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXNotSupportedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Document parseDOM(InputStream inputStream, OutputStream out) throws IOException
	{
		InputSource input = new InputSource(inputStream);
		return parseDOM(input, out);
	}

	@Override
	public Document parseDOM(Reader reader, OutputStream out) throws IOException
	{
		InputSource input = new InputSource(reader);
		return parseDOM(input, out);
	}

	private Document parseDOM(InputSource input, OutputStream out) throws IOException
	{
		if(out != null)
		{
			XMLDocumentFilter writer = new Writer();
			XMLDocumentFilter[] filters = { writer };
			try
			{
				parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
			}
			catch (SAXNotRecognizedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (SAXNotSupportedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		try
		{
			parser.parse(input);
		}
		catch (SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parser.getDocument();
	}
	
	@Override
	public String xPathTagNamesToLower(String xpath)
	{

		StringBuilder newXpath = new StringBuilder();
		
		boolean isTagName = true;
		boolean isAxis 		= false;
		
		for (int i = 0; i < xpath.length(); i++)
		{
			char c 						= xpath.charAt(i);
			char specialChar 	= '\0';

			if (!Character.isLetterOrDigit(c))
				isTagName = false;
			else if (i > 0 && (xpath.charAt(i - 1) == '/' || xpath.charAt(i - 1) == ':'))
			{
				
				String restOfXpath 		= xpath.substring(i + 1);
				int specialCharIndex 	= 0;
				int slash 						= restOfXpath.indexOf('/');
				int j									= 0;

				for (j = 0; j < restOfXpath.length(); j++)
				{
					if (!Character.isLetterOrDigit(restOfXpath.charAt(j)) && restOfXpath.charAt(j) != '/')
					{
						specialCharIndex = j;
						break;
					}
				}
				if (specialCharIndex != 0)
				{
					specialChar = restOfXpath.charAt(j);
					if ((specialChar == ':' || specialChar == '(' || specialChar == '-')
							&& (slash == -1 || slash > specialCharIndex))
						isAxis = true;
				}
			}
			if (c == ':' && specialChar != '(')
				isAxis = false;
			if (i > 0 && (xpath.charAt(i - 1) == '/' || xpath.charAt(i - 1) == ':')
					&& Character.isLetter(c) && !isAxis)
				isTagName = true;

			if (isTagName)
				newXpath.append(Character.toUpperCase(c));
			else
				newXpath.append(c);
		}
		
		return newXpath.toString();
	}

	@Override
	public void setQuiet(boolean b)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setShowWarnings(boolean b)
	{
		// TODO Auto-generated method stub
		
	}

}
