package ecologylab.semantics.html.documentstructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.generic.Debug;
import ecologylab.generic.IntSlot;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.documentparsers.HTMLParserCommon;
import ecologylab.semantics.html.DOMWalkInformationTagger;
import ecologylab.semantics.html.HTMLElementDOM;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.DOMParserInterface;
import ecologylab.semantics.html.utils.HTMLAttributeNames;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.model.text.TermVector;
import ecologylab.serialization.XMLTools;



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
public class RecognizedDocumentStructure extends Debug
implements HTMLAttributeNames
{
	static final int PARAGRAPH_COUNT_MINI_ARTICLE_THRESHOLD = 2;

	static final int PARAGRAPH_COUNT_ARTICLE_THRESHOLD 			= 5;

	static final int CHAR_COUNT_ARTICLE_THRESHOLD						= 300;
	
	ParsedURL				 purl;

	public RecognizedDocumentStructure(ParsedURL purl)
	{
		this.purl			= purl;
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
	public void generateSurrogates(Node articleMain, ArrayList<ImgElement> imgNodes, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTexts, DOMParserInterface htmlType)
	{
		constructImgSurrogatesForOtherPages( imgNodes, totalTxtLeng, htmlType );

		htmlType.setIndexPage();
	}


	/**
	 * Check whether there is an article part in the current document. 
	 * Returns the article part if found. 
	 * 
	 * @param taggedDoc
	 * @return
	 */
	public static Node recognizeContentBody(DOMWalkInformationTagger taggedDoc) 
	{
		Node grandParent	= null;
		Node ggParent 		= null;

		/*
		 * grandParent node with the count information.
		 * 
		 */
		HashMap<Node, IntSlot> grandParentChildCounts = new HashMap<Node, IntSlot>(); 

		/*
		 * grand-grandParent node with the count information.
		 */
		HashMap<Node, IntSlot> greatGrandParentChildCounts = new HashMap<Node, IntSlot>();

		// get linearlized from TreeMap
		Collection<ParagraphText> paragrphTextsValues = taggedDoc.getParagraphTextsTMap().values();
		for(ParagraphText pt : paragrphTextsValues)
		{
			Node parent	= pt.getNode().getParentNode();
			grandParent 	= parent.getParentNode();
			ggParent 		= grandParent.getParentNode();

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

		Node articleMainNode = findArticleMainNode(taggedDoc, grandParentChildCounts /*, paragraphTextsArray */);

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
	private static Node findArticleMainNode(DOMWalkInformationTagger taggedDoc,
			HashMap<Node, IntSlot> ancestorChildCounts /* , Object[] paragraphTextsArray */) 
	{
		Node articleMainNode = null;

		Set<Node> grandParents = ancestorChildCounts.keySet();
		for (Node grandParentNode : grandParents)
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
	protected void associateImageTextSurrogates(DOMParserInterface htmlType, Node articleBody, TreeMap<Integer, ParagraphText> paraTexts)
	{	
		for (ImgElement imgElement: imgNodesInContentBody) 
		{  		
			if (imgElement.isInformativeImage())
			{
				final Node imageNodeNode = imgElement.getNode();
				informImgNodes.add(imageNodeNode);

				StringBuilder extractedCaption = getConnectedText(imageNodeNode, null);	// returns null in worst case
				TermVector captionTV 		= null;
				if (extractedCaption != null)
				{
					XMLTools.unescapeXML(extractedCaption);
					captionTV 						= new TermVector(extractedCaption);
				}
				String altText 					= imgElement.getNonBogusAlt();
				TermVector altTextTV		= (altText == null) ? null : new TermVector(altText);
				boolean done						= false;		// use this instead of break to make sure we get to pt.recycle()
				
				while (!done && (paraTexts.size() > 0))
				{
					ParagraphText pt 	= paraTexts.remove(paraTexts.lastKey());	// get longest remaining paragraph
					Node textNode 	= pt.getNode();
					if (textNode.getParentNode().getParentNode().equals(articleBody) || 
							textNode.getParentNode().getParentNode().getParentNode().equals(articleBody) )
					{
//						if (pt.hasText()) // should be no longer necessary -- andruid 8/09
//						{
						pt.unescapeXML();			
						boolean setAltToCaption							= false;
						if ((captionTV != null) || (altText!=null))
						{
							TermVector ptTV					= pt.termVector();	// this is a candidate text context
							double captionDotTextContext			= 0;
							if (captionTV != null)
							{
								//									imageNode.setAttribute(EXTRACTED_CAPTION, StringTools.toString(extractedCaption));
								captionDotTextContext 					= captionTV.dot(ptTV);
							}

							double altDotTextContext					= 0;
							if (altText!=null)
							{
								altDotTextContext								= altTextTV.dot(ptTV);
							}
							// check for common sharp terms between associateText and captionText
							if ((captionDotTextContext > 0) || (altDotTextContext > 0))
							{
								pt.setImgElementTextContext(imgElement);
								if (captionDotTextContext > altDotTextContext)
								{
									imgElement.setAlt(StringTools.toString(extractedCaption));
									imgElement.setExtractedCaption(pt.getBuffy().toString());
									setAltToCaption									= true;
								}
								done															= true;
							}
							ptTV.clear();
						}
						else
						{	// no alt attribute or extracted caption, so use the first (longest) text context
							// FIXME -- should we try dot product with title?!
							pt.setImgElementTextContext(imgElement);
							done																= true;
						}
						if (!setAltToCaption && (extractedCaption != null))
							imgElement.setExtractedCaption(StringTools.toString(extractedCaption));
								
//						} // if pt.hasText
					}   // if grandParent or greatGrandParent is articleBody
					pt.recycle();
				}	// end while (!done && (paraTexts.size() > 0))
				
				if (extractedCaption != null)
				{
					StringBuilderUtils.release(extractedCaption);
					captionTV.clear();
				}
				if (altTextTV != null)
					altTextTV.clear();

				ParsedURL anchorPurl = findAnchorPURL(imgElement);

				htmlType.newImgTxt(imgElement, anchorPurl);
			} // if isInformImage
		} // for each imageNode in content body
	}

	/**
	 * Recognize the image surrogate for the other page based on the link to the other document (checking mime type for the page)
	 * and nearby text whether the text is informative and can be associated with the image for the image+text surrogate. 
	 * 
	 */
	protected void constructImgSurrogatesForOtherPages(ArrayList<ImgElement> imgNodes, int totalTxtLeng, DOMParserInterface htmlType)
	{    	
		for (ImgElement imgElement : imgNodes)
		{
			if (HTMLParserCommon.isAd(imgElement.getSrc()))
				continue;
			
			Node imgNodeNode							= imgElement.getNode();
			//TODO -- can make this search for text context more comprehensive, while making sure to stay out of content body
			StringBuilder extractedContext	= getLongestTxtinSubTree(imgNodeNode.getParentNode().getParentNode(), null);
			String alt											= imgElement.getAlt();
			// this if condition checks whether the nearest text to the image is substantial enough to form a surrogate. 
			// TODO needs to check parent Href and Text informativity
			if (extractedContext != null || alt != null)
			{
				boolean useContext = extractedContext != null && (extractedContext.length()>10) && (!StringTools.contains(extractedContext, "advertis"));
				if ((alt != null && alt.length() > 10) || useContext)
				{
					ParsedURL anchorPurl 			= findAnchorPURL(imgElement);
	
					// Check whether the anchor mimetype is not an image. 
					if( (anchorPurl!=null) && !anchorPurl.isImg() )
					{
						// TODO!! ask whether we should add this to the associateText or not.
						//FIXME! -- push caption text through as StringBuilder!
						if (useContext)
							imgElement.setTextContext(extractedContext);
	
						htmlType.newAnchorImgTxt(imgElement, anchorPurl);
//						htmlType.removeTheContainerFromCandidates(anchorPurl);
					}
				}
				StringBuilderUtils.release(extractedContext);
			}
		}
	}
	/**
	 * Check whether the image node has the anchor url or not, if so return it as ParsedURL. 
	 * @param ina
	 *  
	 * @return
	 */
	protected ParsedURL findAnchorPURL(HTMLElementDOM ina) 
	{
		Node aNode		= ina.getNode().getParentNode();
		ParsedURL result= null;
		Node aHref		= null;

		if ("a".equals(aNode.getNodeName()))
			aHref	= aNode.getAttributes().getNamedItem("href");
		else
		{
			aNode					= aNode.getParentNode();
			aHref					= aNode.getAttributes().getNamedItem("href");
		}
		if (aHref != null)
		{
			String hrefValue = aHref.getNodeValue();
			hrefValue			= XMLTools.unescapeXML(hrefValue);
			result				= purl.createFromHTML(hrefValue);
		}
		return result;
	}


	/**
	 * All the article images that determined informative.
	 */
	private ArrayList<Node> informImgNodes		= new ArrayList<Node>();    

	/**
	 * All the image nodes under the sub-tree of the ArticleMain node.
	 */
	protected ArrayList<ImgElement> imgNodesInContentBody = new ArrayList<ImgElement>();

	/**
	 * Finding image nodes under the content body. 
	 * 
	 * @param contentBody
	 * @param imgNodes TODO
	 */
	
	public void findImgsInContentBodySubTree(Node contentBody, ArrayList<ImgElement> imgNodes)
	{
		StringBuilder buffy				= StringBuilderUtils.acquire();
		xpath(buffy, contentBody);
		String contentBodyXpath		= buffy.toString();
		StringBuilderUtils.release(buffy);
		
		int i											= imgNodes.size();
		while (--i >= 0)
		{
			ImgElement imgNode			= imgNodes.get(i);
			String imgXpath					= imgNode.xpath();
			if (imgXpath.startsWith(contentBodyXpath))
			{
				imgNodes.remove(i);
				imgNodesInContentBody.add(imgNode);
			}
		}
	}
	
	public void xpath(StringBuilder buffy, Node node)
	{
		if (node.getParentNode() != null && node.getParentNode().getNodeName() != null)
			xpath(buffy, node.getParentNode());
		thisNodeXPath(buffy, node);
	}

	public void thisNodeXPath(StringBuilder buffy, Node node)
	{
		buffy.append('/').append(node.getNodeName());
		int count = 1;
		Node prev = node.getPreviousSibling();
		while (prev != null)
		{
			if (node.getNodeName().equals(prev.getNodeName()))
				count++;
			prev = prev.getPreviousSibling();
		}
		if (count > 1)
			buffy.append('[').append(count).append(']');
	}
	/**
	 * Common method to find a particular html node based on nodeElementString 
	 * that adds to either hrefNodesInContentBody or imgNodesInContentBody
	 * @param contentBody
	 * @param nodeElementString
	 * @param nodesInContentBody
	 */
	private void htmlNodesInContentBody(Node contentBody,
			String nodeElementString,
			ArrayList<ImgElement> nodesInContentBody)
	{
		NodeList children = contentBody.getChildNodes();
		for (int i=0; i<children.getLength(); i++)
		{
			Node contentNode = children.item(i);
			htmlNodesInContentBody(contentNode, nodeElementString, nodesInContentBody);
			if( contentNode.getNodeName()!=null && contentNode.getNodeName().equals(nodeElementString) )
			{
				ImgElement ina = new ImgElement(contentNode, purl);
				nodesInContentBody.add(ina);
			}
		}
	}

	public static StringBuilder getConnectedText(Node node, StringBuilder textResult)
	{
		Node grandParent		= node.getParentNode().getParentNode();
		StringBuilder	result	= getLongestTxtinSubTree(grandParent, textResult);
		if (result == null || result.length() > 5)
			result							= getLongestTxtinSubTree(grandParent.getParentNode(), textResult);
		return result;
	}
	/**
	 * check the texts under the DOM node that is passed as a parameter.
	 * 
	 * @param parent node of the image node is passed in to the parameter. 
	 */
	public static StringBuilder getLongestTxtinSubTree(Node blockNode, StringBuilder textResult)
	{
		NodeList children = blockNode.getChildNodes();
		for (int i=0; i<children.getLength(); i++)
		{
			Node childNode = children.item(i);
			if( (childNode.getNodeType() != Node.TEXT_NODE) && (childNode.getNodeName()!=null) && (!childNode.getNodeName().equals("script")))
			{
				//Recursive call with the childNode
				textResult = getLongestTxtinSubTree(childNode, textResult);
			}	
			else if (childNode.getNodeType() == Node.TEXT_NODE )
			{
				int curLength	= (textResult == null) ? 0 : textResult.length();
				textResult		= StringBuilderUtils.trimAndDecodeUTF8(textResult, childNode, curLength);
			}
		}
		return textResult;
	}

	protected boolean checkLinkIn(Node parentNode, Node currentNode)
	{
		//  	System.out.println("Parent Node : " + parentNode.element + " : " + currentNode );
		//  	System.out.println("\nCurrentNode: " + parentNode.element );
		Node temp = parentNode.getFirstChild();
		Node prevNode = null;
		while( temp != null )
		{
			/*
    		checkLinkIn(temp, temp);
			if( temp.element != null )    		
				System.out.println("NODE:" + temp.element);
			 */
			if( (prevNode!=null) && (prevNode.getNodeName()!=null) && (prevNode.getNodeName().equals("a")) )
				return true;

			prevNode = temp;
			temp = temp.getFirstChild();
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
	public ArrayList<ImgElement> getImgNodesInContentBody() 
	{
		return imgNodesInContentBody;
	}

	public ArrayList<Node> getInformImgNodes() 
	{
		return informImgNodes;
	}

	@Override
	public String toString()
	{
		return super.toString() + "[" + purl + "]";
	}



}