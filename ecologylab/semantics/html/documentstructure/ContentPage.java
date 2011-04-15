package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.tidy.TdNode;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.TidyInterface;


/**
 * Generate surrogates for the documents that are determined as ContentPage.
 * 
 * @author eunyee
 *
 */
public class ContentPage extends RecognizedDocumentStructure
{
	public ContentPage(ParsedURL purl)
	{
		super(purl);
	}

	/**
	 * This is the case that the page is a content page or an index-content page. 
	 * 
	 * 1. Generate Image+Text surrogate inside the ArticleMain body. 
	 * 2. Generate surrogates for other pages (with those image surrogates in the bottom or side of the pages)
	 */
	@Override
	public void generateSurrogates(TdNode contentBody, ArrayList<ImgElement> imgNodes, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTexts, TidyInterface htmlType)
	{
		findImgsInContentBodySubTree(contentBody.parent(), imgNodes); 
		associateImageTextSurrogates(htmlType, contentBody, paraTexts);	// removes from imgNodes
		htmlType.setContent();

		constructImgSurrogatesForOtherPages( imgNodes, totalTxtLeng, htmlType );	// act just on leftover img nodes
	}
}