package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.w3c.tidy.Lexer;
import org.w3c.tidy.TdNode;

import ecologylab.semantics.html.HTMLElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.RecognizedDocumentStructure;
import ecologylab.semantics.html.TidyInterface;


/**
 * Generate surrogates for the documents that are determined as ContentPage.
 * 
 * @author eunyee
 *
 */
public class ContentPage extends RecognizedDocumentStructure
{
	/**
	 * 1. Generate surrogates for other pages (with those image surrogates in the bottom or side of the pages)
	 * 2. Generate Image+Text surrogate inside the ArticleMain body. 
	 */
	protected void generateSurrogates(TdNode articleMain, ArrayList<HTMLElement> imgNodes, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTexts, TidyInterface htmlType)
	{
    	// This is the case there is no article main, which means high probability to be an index page.
        // Needs to author informative image and text surrogate in the whole document itself.
        recognizeImgSurrogateForOtherPages( imgNodes, totalTxtLeng, htmlType );	
        
        // This is the case that the page is a content page or an index-content page. 
        if( (articleMain!=null) && (articleMain.parent()!=null) /*&& (!articleMain.parent().equals(document))*/ )
        {
       		// Recursively add images inside the articleMain DOM into the imgNodes HashMap.
       		findImgsInContentBodySubTree(articleMain.parent());
       		associateImageTextSurrogate(htmlType, articleMain, paraTexts);
       		htmlType.setContent();
        }
	}
	

	
}