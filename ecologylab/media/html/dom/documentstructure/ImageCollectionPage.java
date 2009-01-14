package ecologylab.media.html.dom.documentstructure;

import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.tidy.TdNode;

import ecologylab.media.html.dom.HTMLDOMParser;
import ecologylab.media.html.dom.ImgNodewithAttr;
import ecologylab.media.html.dom.ParagraphText;
import ecologylab.media.html.dom.RecognizedDocumentStructure;
import ecologylab.media.html.dom.TidyInterface;
import ecologylab.net.ParsedURL;


/**
 * Generate surrogates for the documents that are determined as Image-Collection Pages.
 * 
 * @author eunyee
 *
 */
public class ImageCollectionPage extends RecognizedDocumentStructure
{
	/**
	 * Generate surrogates for the images inside the image-collection pages. 
	 */
	protected void generateSurrogates(TdNode articleMain, ArrayList<ImgNodewithAttr> imgNodes, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTexts, TidyInterface htmlType)
	{
    	for(int i=0; i<imgNodes.size(); i++)
    	{
    		ImgNodewithAttr ina = (ImgNodewithAttr)imgNodes.get(i);
	    	
    		ParsedURL anchorPurl = findAnchorPURLforImgNode(htmlType, ina);
    		
    		// images in the image-collection pages won't have anchors 
    		// If there is an anchor, it should be pointing to the bigger image. 
    		if(anchorPurl==null)
    			htmlType.newImgTxt(ina.getAttributesMap(), null);
    		else if( (anchorPurl!=null) && anchorPurl.isImg() )
    		{
        		htmlType.newImgTxt(ina.getAttributesMap(), anchorPurl);
				htmlType.removeTheContainerFromCandidates(anchorPurl);
    		}
    		else if(anchorPurl.isHTML() || anchorPurl.isPDF() || anchorPurl.isRSS())
    		{
    			//TODO find the anchorContext for this purl
    		}

    	}
	}
}