package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.tidy.TdNode;

import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.HTMLDOMParser;
import ecologylab.semantics.html.HTMLElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.RecognizedDocumentStructure;
import ecologylab.semantics.html.TidyInterface;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.xml.XMLTools;

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
	protected void generateSurrogates(TdNode articleMain, ArrayList<HTMLElement> imgNodes,
			int totalTxtLeng, TreeMap<Integer, ParagraphText> paraTexts, TidyInterface htmlType)
	{
		for (int i = 0; i < imgNodes.size(); i++)
		{
			HTMLElement imageNode = (HTMLElement) imgNodes.get(i);
			
			String altText 					= ImageFeatures.getNonBogusAlt(imageNode);
			
			if (altText == null)
			{
				final TdNode imageNodeNode = imageNode.getNode();
				StringBuilder extractedCaption = getLongestTxtinSubTree(imageNodeNode.grandParent(), null);	// returns null in worst case
				if (extractedCaption == null)
					extractedCaption = getLongestTxtinSubTree(imageNodeNode.greatGrandParent(), null);	// returns null in worst case
								
				if (extractedCaption != null)
				{
					XMLTools.unescapeXML(extractedCaption);
					imageNode.setAttribute(ALT, StringTools.toString(extractedCaption));
					
					StringBuilderUtils.release(extractedCaption);
				}
			}

			ParsedURL anchorPurl = findAnchorPURLforImgNode(htmlType, imageNode);

			// images in the image-collection pages won't have anchors
			// If there is an anchor, it should be pointing to the bigger image.
			if (anchorPurl == null)
				htmlType.newImgTxt(imageNode, null);
			else if ((anchorPurl != null) && anchorPurl.isImg())
			{
				htmlType.newImgTxt(imageNode, anchorPurl);
				htmlType.removeTheContainerFromCandidates(anchorPurl);
			}
			else if (anchorPurl.isHTML() || anchorPurl.isPDF() || anchorPurl.isRSS())
			{
				// TODO find the anchorContext for this purl
			}
			imageNode.recycle();
		}
	}
}