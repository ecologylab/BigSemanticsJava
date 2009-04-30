package ecologylab.semantics.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.tidy.AttVal;
import org.w3c.tidy.Lexer;
import org.w3c.tidy.TdNode;

import ecologylab.generic.Generic;
import ecologylab.generic.IntSlot;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.html.utils.HTMLAttributeNames;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.xml.XMLTools;



/**
 * We recognize web pages to index page, index-content page, content page, image-collection page, and low-quality page 
 * to work in forming surrogates. 
 * 
 * The image-collection page has lots of images with very few text. Those images may link to the Image URL or not link to anything else, 
 * so we are looking at the page mime type. 
 * 
 * In the index-content page, images are formed as surrogates if it is linked to the document, and there is informative text nearby 
 * that image. 
 * 
 * @author eunyee
 * @author andruid
 */
public class RecognizedDocumentStructure
implements HTMLAttributeNames
{
	static final int PARAGRAPH_COUNT_MINI_ARTICLE_THRESHOLD = 2;

	static final int PARAGRAPH_COUNT_ARTICLE_THRESHOLD 			= 5;

	static final int CHAR_COUNT_ARTICLE_THRESHOLD						= 300;

	public RecognizedDocumentStructure()
	{

	}

	/**
	 * This is the case there is no article main, which means high probability to be an index page.
	 * Needs to author informative image and text surrogate in the whole document itself.
	 * 
	 * @param articleMain
	 * @param imgNodes   
	 * @param totalTxtLeng
	 * @param paraTexts  
	 * @param htmlType
	 */
	protected void generateSurrogates(TdNode articleMain, ArrayList<HTMLElement> imgNodes, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTexts, TidyInterface htmlType)
	{
		recognizeImgSurrogateForOtherPages( imgNodes, totalTxtLeng, htmlType );

		htmlType.setIndexPage();
	}

	/**
	 * Recognize the image surrogate for the other page based on the link to the other document (checking mime type for the page)
	 * and nearby text whether the text is informative and can be associated with the image for the image+text surrogate. 
	 * 
	 */
	protected void recognizeImgSurrogateForOtherPages(ArrayList<HTMLElement> imgNodes, int totalTxtLeng, TidyInterface htmlType)
	{    	
		for(HTMLElement imgNode : imgNodes)
		{
			TdNode imgNodeNode					= imgNode.getNode();
			StringBuilder extractedCaptionTxt	= getLongestTxtinSubTree(imgNodeNode.grandParent(), null);

			// this if condition checks whether the nearest text to the image is substantial enough to form a surrogate. 
			// TODO needs to check parent Href and Text informativity
			if (extractedCaptionTxt != null)
			{
				if ((extractedCaptionTxt.length()>10) && (!StringTools.contains(extractedCaptionTxt, "advertis")) )
				{
					ParsedURL anchorPurl 			= findAnchorPURLforImgNode(htmlType, imgNode);
	
					// Check whether the anchor mimetype is not an image. 
					if( (anchorPurl!=null) && !anchorPurl.isImg() )
					{
						// TODO!! ask whether we should add this to the associateText or not.
						//FIXME! -- push caption text through as StringBuilder!
						imgNode.setAttribute(TEXT_CONTEXT, StringTools.toString(extractedCaptionTxt));
	
						htmlType.newAnchorImgTxt(imgNode, anchorPurl);
						htmlType.removeTheContainerFromCandidates(anchorPurl);
					}
				}
				StringBuilderUtils.release(extractedCaptionTxt);
			}
		}
	}

	/**
	 * Check whether there is an article part in the current document. 
	 * Returns the article part if found. 
	 * 
	 * @param taggedDoc
	 * @return
	 */
	public static TdNode recognizeContentBody(DOMWalkInformationTagger taggedDoc) 
	{
		TdNode grandParent	= null;
		TdNode ggParent 		= null;

		/*
		 * grandParent node with the count information.
		 * 
		 */
		HashMap<TdNode, IntSlot> grandParentChildCounts = new HashMap<TdNode, IntSlot>(); 

		/*
		 * grand-grandParent node with the count information.
		 */
		HashMap<TdNode, IntSlot> greatGrandParentChildCounts = new HashMap<TdNode, IntSlot>();

		// get linearlized from TreeMap
		Collection<ParagraphText> paragrphTextsValues = taggedDoc.getParagraphTextsTMap().values();
		for(ParagraphText pt : paragrphTextsValues)
		{
			TdNode parent	= pt.getNode().parent();
			grandParent 	= parent.parent();
			ggParent 		= grandParent.parent();

			// FIXME: Refactor the below method
			//identify common grandParent
			if( grandParentChildCounts.containsKey(grandParent) )
			{
				IntSlot numProgenySoFar = grandParentChildCounts.get(grandParent);
				numProgenySoFar.value++;
			}
			else
				grandParentChildCounts.put(grandParent, new IntSlot(1));

			//identify common great grandParent
			if( greatGrandParentChildCounts.containsKey(ggParent) )
			{
				IntSlot numProgenySoFar = greatGrandParentChildCounts.get(ggParent);
				numProgenySoFar.value++;
			}
			else
				greatGrandParentChildCounts.put(ggParent, new IntSlot(1));

		}
//		Object[] paragraphTextsArray = paragrphTextsValues.toArray();

		TdNode articleMainNode = findArticleMainNode(taggedDoc, grandParentChildCounts /*, paragraphTextsArray */);

		// if no common grandParent, look for common greatGrandParent. 
		if( articleMainNode == null )
		{
			articleMainNode = findArticleMainNode(taggedDoc, greatGrandParentChildCounts /*, paragraphTextsArray */);
		}

		return articleMainNode;
	}

	/**
	 * identify article sub-tree by locating common ancestor. 
	 * (Eunyee's dissertation algorithm) 
	 * 
	 * @param taggedDoc
	 * @param ancestorChildCounts
	 * @param paragraphTextsArray
	 * @return
	 */
	private static TdNode findArticleMainNode(DOMWalkInformationTagger taggedDoc,
			HashMap<TdNode, IntSlot> ancestorChildCounts /* , Object[] paragraphTextsArray */) 
	{
		TdNode articleMainNode = null;

		Set<TdNode> grandParents = ancestorChildCounts.keySet();
		for (TdNode grandParentNode : grandParents)
		{
			IntSlot tint 	= ancestorChildCounts.get(grandParentNode);

			// If the majority of the paragraph nodes has the common grandParent node, 
			// we recognize the grandParent node as an articleMain node.
			if( tint.value >= PARAGRAPH_COUNT_ARTICLE_THRESHOLD )
			{
				articleMainNode = grandParentNode;
				break;
			}
			/*
			else if( tint.value >= PARAGRAPH_COUNT_MINI_ARTICLE_THRESHOLD )
			{
				int size 			= pprint.paragraphTexts.size();
				ParagraphText pt1 	= (ParagraphText) paragraphTextsArray[size-1];
				ParagraphText pt2 	= (ParagraphText) paragraphTextsArray[size-2];			

				if( (pt1.ptext.length() > CHAR_COUNT_ARTICLE_THRESHOLD) &&
					(pt2.ptext.length() > CHAR_COUNT_ARTICLE_THRESHOLD))
				{
						articleMainNode = grandParentNode;
						break;
				}

				//else if(pt1.ptext.length()+pt2.ptext.length()>500)
				//{
				//	articleMainNode = grandParentNode;
				//}

			}
			 */
		}
		return articleMainNode;
	}
	
	/**
	 * 1)	Image should be not too small (small images are usually copyrights or icons..)
	 * 2)	Image with a link does not tend to be an article image. 
	 * 		It may be informative-image, but it is not an article-related image. 
	 * 3)	Image ratio can sometimes catch uninformative images. 
	 * 4)	Textual Features: terms in URL, Alt texts, descriptions, and nearest texts in DOM. 
	 * 
	 */
	//FIXME -- andruid: exactly what sorted order are the paraTexts in? why?
	protected void associateImageTextSurrogate(TidyInterface htmlType, TdNode articleBody, TreeMap<Integer, ParagraphText> paraTexts)
	{	
		for (HTMLElement imageNode: imgNodesInContentBody) 
		{  		
			if (ImageFeatures.isInformativeImage(imageNode))
			{
				final TdNode imageNodeNode = imageNode.getNode();
				informImgNodes.add(imageNodeNode);

				StringBuilder extractedCaption = getLongestTxtinSubTree(imageNodeNode.grandParent(), null);	// returns null in worst case
				if (extractedCaption != null)
					XMLTools.unescapeXML(extractedCaption);
				
				String altText 					= ImageFeatures.getNonBogusAlt(imageNode);
				TermVector altTextTV		= (altText == null) ? null : new TermVector(altText);
				boolean done						= false;		// use this instead of break to make sure we get to pt.recycle()
				
				while (!done && (paraTexts.size() > 0))
				{
					ParagraphText pt 	= paraTexts.remove(paraTexts.lastKey());	// get longest remaining paragraph
					TdNode textNode 	= pt.getNode();
					if (textNode.grandParent().equals(articleBody) || 
							textNode.greatGrandParent().equals(articleBody) )
					{
						StringBuilder textContext = pt.getPtext();
						
						if (textContext != null)
						{
							if ((extractedCaption != null) || (altText!=null))
							{
								TermVector textContextTV					= new TermVector(textContext);
								double captionDotTextContext			= 0;
								if (extractedCaption != null)
								{
//									imageNode.setAttribute(EXTRACTED_CAPTION, StringTools.toString(extractedCaption));
									TermVector captionTV 						= new TermVector(extractedCaption);
									captionDotTextContext 					= captionTV.dot(textContextTV);
								}

								double altDotTextContext					= 0;
								if (altText!=null)
								{
									altDotTextContext								= altTextTV.dot(textContextTV);
								}
								// check for common sharp terms between associateText and captionText
								if ((captionDotTextContext > 0) || (altDotTextContext > 0))
								{
									XMLTools.unescapeXML(textContext);			
									imageNode.setAttribute(HTMLAttributeNames.TEXT_CONTEXT, StringTools.toString(textContext));
									if (captionDotTextContext > altDotTextContext)
										imageNode.setAttribute(ALT, StringTools.toString(extractedCaption));
									done															= true;
								}
							}
							else
							{	// no alt attribute or extracted caption, so use the first (longest) text context
								// FIXME -- should we try dot product with title?!
								XMLTools.unescapeXML(textContext);
								imageNode.setAttribute(TEXT_CONTEXT, StringTools.toString(textContext));
								done																= true;
							}
						} // if (textContext != null
					}   // if grandParent or greatGrandParent is articleBody
					pt.recycle();
				}	// end while (!done && (paraTexts.size() > 0))
				
				if (extractedCaption != null)
					StringBuilderUtils.release(extractedCaption);

				ParsedURL anchorPurl = findAnchorPURLforImgNode(htmlType, imageNode);

				// removed by andruid 10/16/08
				// 1) i dont see why we need to do this.
				// 2) IT CREATES A RACE CONDITION WITH PRUNE, WHICH STOPS THE CRAWLER :-(
				//        		if( anchorPurl!=null )
				//        			htmlType.removeTheContainerFromCandidates(anchorPurl);

				htmlType.newImgTxt(imageNode, anchorPurl);
			} // if isInformImage
		} // for each imageNode in content body
	}

	/**
	 * Check whether the image node has the anchor url or not, if so return it as ParsedURL. 
	 *  
	 * @param htmlType
	 * @param ina
	 * @return
	 */
	protected ParsedURL findAnchorPURLforImgNode(TidyInterface htmlType, HTMLElement ina) 
	{
		boolean isparentHref = ina.getNode().parent().element.equals("a"); 
		ParsedURL anchorPurl = null;
		if(isparentHref)
		{
			AttVal parentHref = ina.getNode().parent().getAttrByName("href");
			if( parentHref!=null )
			{
				anchorPurl = htmlType.getAnchorParsedURL(parentHref.value);
			}
			else
			{
				// probably the case that the anchor is pointing to the section in the HTML page
				// For example, <a name="">
			}
		}
		return anchorPurl;
	}


	/**
	 * All the article images that determined informative.
	 */
	private ArrayList<TdNode> informImgNodes		= new ArrayList<TdNode>();    

	/**
	 * All the image nodes under the sub-tree of the ArticleMain node.
	 */
	protected ArrayList<HTMLElement> imgNodesInContentBody = new ArrayList<HTMLElement>();

	/**
	 * Finding image nodes under the content body. 
	 * 
	 * @param contentBody
	 */
	public void findImgsInContentBodySubTree(TdNode contentBody)
	{
		String nodeElementString = "img";
		htmlNodesInContentBody(contentBody, nodeElementString, imgNodesInContentBody);
	}

	/**
	 * Common method to find a particular html node based on nodeElementString 
	 * that adds to either hrefNodesInContentBody or imgNodesInContentBody
	 * @param contentBody
	 * @param nodeElementString
	 * @param nodesInContentBody
	 */
	private void htmlNodesInContentBody(TdNode contentBody,
			String nodeElementString,
			ArrayList<HTMLElement> nodesInContentBody)
	{
		for (TdNode contentNode = contentBody.content(); contentNode != null; contentNode = contentNode.next())
		{
			htmlNodesInContentBody(contentNode, nodeElementString, nodesInContentBody);
			if( contentNode.element!=null && contentNode.element.equals(nodeElementString) )
			{
				HTMLElement ina = new HTMLElement(contentNode);
				nodesInContentBody.add(ina);
			}
		}
	}

	/**
	 * check the texts under the DOM node that is passed as a parameter.
	 * 
	 * @param parent node of the image node is passed in to the parameter. 
	 */
	//FIXME -- use StringBuilder or CharBuffer, not String
	public StringBuilder getLongestTxtinSubTree(TdNode node, StringBuilder textResult)
	{
		for (TdNode childNode	= node.content(); childNode != null; childNode = childNode.next())
		{
			if( (childNode.element!=null) && (!childNode.element.equals("script")))
			{
				//Recursive call with the childNode
				textResult = getLongestTxtinSubTree(childNode, textResult);
			}	
			else if (childNode.type == TdNode.TextNode )
			{
				int curLength	= (textResult == null) ? 0 : textResult.length();
				textResult		= StringBuilderUtils.trimAndDecodeUTF8(textResult, childNode, curLength);
			}
		}
		return textResult;
	}

	protected boolean checkLinkIn(TdNode parentNode, TdNode currentNode)
	{
		//  	System.out.println("Parent Node : " + parentNode.element + " : " + currentNode );
		//  	System.out.println("\nCurrentNode: " + parentNode.element );
		TdNode temp = parentNode.content();
		TdNode prevNode = null;
		while( temp != null )
		{
			/*
    		checkLinkIn(temp, temp);
			if( temp.element != null )    		
				System.out.println("NODE:" + temp.element);
			 */
			if( (prevNode!=null) && (prevNode.element!=null) && (prevNode.element.equals("a")) )
				return true;

			prevNode = temp;
			temp = temp.next();
		}
		return false;
	}



	/**
	 * Initial Implementation for PhatSurrogate Implementation. 
	 * 
	 * @param articleMain
	 */
	//TODO -- get rid of this dead code
	/*
	protected void printArticleText( TdNode articleMain )//, String paraElement)
	{
		if( articleMain!=null && (articleMain.element!=null) && !articleMain.element.equals("script") )
		{
			TdNode temp = articleMain.content();
			while( temp != null )
			{
				//System.out.println("\n\n---------- Paragraph HTML Element : " + paraElement );   		
				printArticleText(temp);
				if( temp.type==TdNode.TextNode ) //&& (temp.parent().element!=null) && temp.parent().element.equals(paraElement))
				{
					// Print Text in ArticleMain
					Lexer.getString(temp.textarray(), temp.start(), temp.end()-temp.start() );   			
				}
				temp = temp.next();
			}
		}
	}
 */
	public ArrayList<HTMLElement> getImgNodesInContentBody() 
	{
		return imgNodesInContentBody;
	}

	public ArrayList<TdNode> getInformImgNodes() 
	{
		return informImgNodes;
	}





}