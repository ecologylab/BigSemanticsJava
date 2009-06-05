package ecologylab.semantics.html.documentstructure;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.RecognizedDocumentStructure;


/**
 * Generate surrogates for the documents that are determined as Index Pages.
 * We only generate surrogates for the other pages from the Index Pages. 
 * 
 * @author eunyee
 *
 */
public class IndexPage extends RecognizedDocumentStructure
{
	public IndexPage(ParsedURL purl)
	{
		super(purl);
	}
}