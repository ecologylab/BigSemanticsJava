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
	 * 1. Generate surrogates for other pages (with those image surrogates in the bottom or side of the pages)
	 * 2. Generate Image+Text surrogate inside the ArticleMain body. 
	 */
	@Override
	public void generateSurrogates(TdNode articleMain, ArrayList<ImgElement> imgNodes, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTexts, TidyInterface htmlType)
	{
		//FIXME -- andruid 4/7/2011 -- this is seriously wrong. i must rework soon!
		// this, if anything, should be performed after associateImageTextSurrogate, and should act only on 
		// the images not processed there!
		
        recognizeImgSurrogateForOtherPages( imgNodes, totalTxtLeng, htmlType );	
        
        // This is the case that the page is a content page or an index-content page. 
        if( (articleMain!=null) && (articleMain.parent()!=null) /*&& (!articleMain.parent().equals(document))*/ )
        {
       		// Recursively add images inside the articleMain DOM into the imgNodes HashMap.
       		findImgsInContentBodySubTree(articleMain.parent());
       		associateImageTextSurrogate(htmlType, articleMain, paraTexts);
       		htmlType.setContent();
        }
        else
        {
        	warning("Content Body but no parent, so not forming image-text surrogates.");
        }
	}
	

	
}