package ecologylab.semantics.documentparsers;

import java.io.InputStream;
import java.util.ArrayList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.html.DOMFragmentInformationTagger;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.TidyInterface;
import ecologylab.semantics.metadata.builtins.Document;

public class HTMLFragmentDOMParser
extends HTMLDOMParser
implements TidyInterface
{

	InputStream fragmentStream;
	public DOMFragmentInformationTagger taggedDoc;
	
	public HTMLFragmentDOMParser(NewInfoCollector infoCollector, InputStream inputStream)
	{
		super(infoCollector);
		fragmentStream = inputStream;
		parse();
	}
	
	public Document doParse()
	{
		taggedDoc = new DOMFragmentInformationTagger(tidy.getConfiguration(), null, this);
		taggedDoc.generateCollections(this);
	}
	
	public InputStream inputStream()
	{
		return fragmentStream;
	}
	
	public String getDNDText()
	{
		return taggedDoc.getDNDText();
	}
	
	public ArrayList<ImgElement> getDNDImages()
	{
		return taggedDoc.getDNDImages();
	}

	public void setContent()
	{	
	}

	public void setIndexPage()
	{	
	}
	
	/**
	 * To make code navigation simpler around JTidy-parsers 
	 */
	@Override
	public void newImgTxt(ImgElement imgNode, ParsedURL anchorHref)
	{
		super.newImgTxt(imgNode, anchorHref);
	}
}
