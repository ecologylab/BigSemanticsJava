package ecologylab.semantics.documentparsers;

import java.io.InputStream;
import java.util.ArrayList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.html.DOMFragmentInformationTagger;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.TidyInterface;
import ecologylab.semantics.metadata.builtins.AnonymousDocument;
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
		fragmentStream 											= inputStream;
		AnonymousDocument anonymousDocument	= new AnonymousDocument();
		this.documentClosure								= anonymousDocument.getOrConstructClosure();
		parse();
	}
	
	@Override
	public Document doParse()
	{
		taggedDoc = new DOMFragmentInformationTagger(tidy.getConfiguration(), null, this);
		taggedDoc.generateCollections(this);
		
		return null;
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

}
