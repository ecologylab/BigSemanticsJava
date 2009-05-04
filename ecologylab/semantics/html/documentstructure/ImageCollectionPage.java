package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.tidy.TdNode;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.HTMLDOMParser;
import ecologylab.semantics.html.HTMLElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.RecognizedDocumentStructure;
import ecologylab.semantics.html.TidyInterface;

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
			HTMLElement ina = (HTMLElement) imgNodes.get(i);

			ParsedURL anchorPurl = findAnchorPURLforImgNode(htmlType, ina);

			// images in the image-collection pages won't have anchors
			// If there is an anchor, it should be pointing to the bigger image.
			if (anchorPurl == null)
				htmlType.newImgTxt(ina, null);
			else if ((anchorPurl != null) && anchorPurl.isImg())
			{
				htmlType.newImgTxt(ina, anchorPurl);
				htmlType.removeTheContainerFromCandidates(anchorPurl);
			}
			else if (anchorPurl.isHTML() || anchorPurl.isPDF() || anchorPurl.isRSS())
			{
				// TODO find the anchorContext for this purl
			}

		}
	}
}