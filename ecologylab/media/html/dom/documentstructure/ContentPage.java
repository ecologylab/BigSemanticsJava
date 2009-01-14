package ecologylab.media.html.dom.documentstructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.w3c.tidy.Lexer;
import org.w3c.tidy.TdNode;

import ecologylab.media.html.dom.ImgNodewithAttr;
import ecologylab.media.html.dom.ParagraphText;
import ecologylab.media.html.dom.RecognizedDocumentStructure;
import ecologylab.media.html.dom.TidyInterface;


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
	protected void generateSurrogates(TdNode articleMain, ArrayList<ImgNodewithAttr> imgNodes, int totalTxtLeng, 
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
        	findHrefsAndContext(articleMain, htmlType);
       		associateImageTextSurrogate(htmlType, articleMain, paraTexts);

        }
	}

	/**
	 * @param articleMain
	 */
	private void findHrefsAndContext(TdNode articleMain, TidyInterface htmlType)
	{
		findAnchorsInContentBodySubTree(articleMain);
		
		ArrayList<ImgNodewithAttr> anchorNodesInContentBody = getAnchorNodesInContentBody();
		ArrayList<AnchorContext> anchorNodeContexts = new ArrayList<AnchorContext>();
		for(ImgNodewithAttr anchorNode : anchorNodesInContentBody)
		{
			TdNode node 				= anchorNode.getNode().parent();
			String tempstr 			= getTextinSubTree(node);
			String anchorText 	= getTextinSubTree(anchorNode.getNode());
			String href 				= (String) anchorNode.getAttributesMap().get("href");
			if(href == null)
				continue;
			anchorNodeContexts.add(new AnchorContext(href,anchorText,tempstr));
			
		}
		//TODO Now add some metadata / weight to this href with some sense.
		//        	htmlType.newAHref(attributesMap);
//		System.out.println("recognized href Elements in contentBody: " + anchorNodeContexts.size());
		if(htmlType != null)
			htmlType.newAHref(anchorNodeContexts);
	}
	

	
}