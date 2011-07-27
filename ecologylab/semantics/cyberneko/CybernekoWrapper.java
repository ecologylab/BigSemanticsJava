package ecologylab.semantics.cyberneko;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


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
		parser = new DOMParser();
	}

	@Override
	public Document parseDOM(InputStream inputStream, OutputStream out) throws IOException
	{
		InputSource input = new InputSource(inputStream);
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
