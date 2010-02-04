package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.w3c.tidy.TdNode;

import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.ImgElement;
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
	public ImageCollectionPage(ParsedURL purl)
	{
		super(purl);
	}

	/**
	 * Generate surrogates for the images inside the image-collection pages.
	 */
	@Override
	public void generateSurrogates(TdNode articleMain, ArrayList<ImgElement> imgElements, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTextMap, TidyInterface htmlType)
	{
		Collection<ParagraphText> paraTextsC	= paraTextMap.values();
		ParagraphText[] paraTexts	= new ParagraphText[paraTextsC.size()];
		paraTextsC.toArray(paraTexts);
		for (int i = 0; i < imgElements.size(); i++)
		{
			ImgElement imgElement 				= imgElements.get(i);
			
			String altText 								= imgElement.getNonBogusAlt();
			
			if (altText == null)
			{
				final TdNode imageNodeNode 	= imgElement.getNode();
				StringBuilder extractedCaption = getLongestTxtinSubTree(imageNodeNode.grandParent(), null);	// returns null in worst case
				if (extractedCaption == null)
					extractedCaption = getLongestTxtinSubTree(imageNodeNode.greatGrandParent(), null);	// returns null in worst case
								
				if (extractedCaption != null)
				{
					XMLTools.unescapeXML(extractedCaption);
					imgElement.setAlt(StringTools.toString(extractedCaption));
					
					StringBuilderUtils.release(extractedCaption);
				}
			}

			ParsedURL anchorPurl = findAnchorPURLforImgNode(htmlType, imgElement);

			// images in the image-collection pages won't have anchors
			// If there is an anchor, it should be pointing to the bigger image.
			if (anchorPurl == null)
				htmlType.newImgTxt(imgElement, null);
			else if ((anchorPurl != null) && anchorPurl.isImg())
			{
				htmlType.newImgTxt(imgElement, anchorPurl);
				htmlType.removeTheContainerFromCandidates(anchorPurl);
			}
			else // if (anchorPurl.isHTML() || anchorPurl.isPDF() || anchorPurl.isRSS())
			{
				// TODO find the anchorContext for this purl
				TdNode parent		= imgElement.getNode().parent();
				TdNode gParent	= parent.parent();
				TdNode ggParent	= gParent.parent();
				for (ParagraphText paraText : paraTexts)
				{
					TdNode paraTextNode	= paraText.getElementNode();
					TdNode contextNode	= null;
					if (paraTextNode == parent)
						contextNode				= parent;
					else if (paraTextNode == gParent)
						contextNode				= gParent;
					else if (paraTextNode == ggParent)
						contextNode				= ggParent;
					else for (TdNode childNode	= paraTextNode.content(); childNode != null; childNode = childNode.next())
					{
						if (paraTextNode == childNode)
						{
							contextNode			= childNode;
						}
					}
					if (contextNode != null)
					{
						paraText.setImgElementTextContext(imgElement);
						break;
					}
				}
				htmlType.newImgTxt(imgElement, anchorPurl);
			}
			imgElement.recycle();
		}
	}
}