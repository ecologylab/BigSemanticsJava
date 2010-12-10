package ecologylab.semantics.documentparsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.w3c.tidy.AttVal;
import org.w3c.tidy.Out;
import org.w3c.tidy.OutImpl;
import org.w3c.tidy.TdNode;

import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.html.AElement;
import ecologylab.semantics.html.DOMWalkInformationTagger;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.ParagraphText;
import ecologylab.semantics.html.TidyInterface;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.semantics.html.documentstructure.ContentPage;
import ecologylab.semantics.html.documentstructure.ImageCollectionPage;
import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.html.documentstructure.IndexPage;
import ecologylab.semantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.semantics.html.documentstructure.TextOnlyPage;
import ecologylab.semantics.html.utils.HTMLAttributeNames;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.serialization.XMLTools;


/**
 * Parse HTML, create DOM, and author Image and Text surrogates from DOM
 * 
 * @author eunyee
 *
 */
public class HTMLDOMImageTextParser<C extends Container>
extends HTMLDOMParser<C, InfoCollector<C>>
implements TidyInterface, HTMLAttributeNames
{
	public HTMLDOMImageTextParser(InfoCollector infoCollector)	// this is of type In
	{
		super(infoCollector);
	}
	
	public HTMLDOMImageTextParser(InfoCollector infoCollector,SemanticActionHandler semanticActionHandler)	// this is of type In
	{
		super(infoCollector, semanticActionHandler);
	}
	
	boolean indexPage = false;
	boolean contentPage = false;
	HashMap<TdNode, String> tdNodeAnchorContextStringCache;
//	@Override
//	public void parse() 
//	{   	
//		HTMLDOMParser parser		= new HTMLDOMParser();
//		try
//		{  
//			// Call HTML DOM Parser that creates pretty DOM and extracts Image and Text surrogates
//			parser.parse(purlConnection(), this);
//		} 
//		catch (Exception e) 
//		{
//			debug("ERROR: while reading - " + e.getMessage());
//			e.printStackTrace();
//		}	
//	}
	@Override
	protected void postParse()
	{
		DOMWalkInformationTagger taggedDoc = new DOMWalkInformationTagger(tidy.getConfiguration(), purlConnection.getPurl(), this);
		// this function actually traverse the dom tree
		taggedDoc.generateCollections(document);
		
		TdNode contentBody = getContentBody(taggedDoc);
		DOMWalkInformationTagger taggedContentnode = walkAndTagDom(contentBody, this);
		
		extractImageTextSurrogates(taggedDoc, contentBody);
		
		
		//The information on whether the anchorContexts where produced by the document entirely or the ContentNode.
		ArrayList<AnchorContext> anchorContexts = null;
		//This document's purl
		ParsedURL purl = purl();
		boolean fromContentBody = taggedContentnode != null;
		if (fromContentBody)
			anchorContexts = buildAnchorContexts(taggedContentnode.getAllAnchorNodes(), purl, true);
		else
			anchorContexts = buildAnchorContexts(taggedDoc.getAllAnchorNodes(), purl, false);
		
  	generateCandidateContainersFromContexts(anchorContexts, fromContentBody);
  	
  	anchorContexts.clear();
		taggedDoc.recycle();
		if (fromContentBody)
			taggedContentnode.recycle();
	}

	
	/**
	 * This is the walk of the dom that calls print tree, and the parser methods such as closeHref etc.
	 * @param doc
	 * @param htmlType
	 * @return
	 */
	public DOMWalkInformationTagger walkAndTagDom(TdNode tdNode, TidyInterface htmlType)
	{
		// note that content body could be null if it is not a content page
		if (tdNode == null)
			return null;
		
		Out jtidyPrettyOutput = new OutImpl();

		DOMWalkInformationTagger domTagger = new DOMWalkInformationTagger(tidy.getConfiguration(), purlConnection.getPurl(), htmlType);
		domTagger.generateCollections(tdNode);
		// walk through the HTML document object.
		// gather all paragraphText and image objects in the data structure.
		//FIXME -- get rid of this call and object!
//		domTagger.printTree(jtidyPrettyOutput, (short)0, 0, null, tdNode);
//		domTagger.flushLine(jtidyPrettyOutput, 0);
		return domTagger;
	}

	/**
	 * Extract Image and Text Surrogates while walk through DOM
	 * 
	 * historically was called as pprint() in JTidy. 
	 */
	public void extractImageTextSurrogates(DOMWalkInformationTagger taggedDoc, TdNode contentBody)
	{

		//System.out.println("\n\ncontentBody = " + contentBody);       
		ArrayList<ImgElement> imgNodes = taggedDoc.getAllImgNodes();

		recognizeDocumentStructureToGenerateSurrogate(taggedDoc, contentBody, imgNodes);
	}

	/**
	 * @param taggedDoc
	 * @return
	 */
	private TdNode getContentBody(DOMWalkInformationTagger taggedDoc)
	{
		TdNode contentBody = RecognizedDocumentStructure.recognizeContentBody(taggedDoc);
		return contentBody;
	}
	
	/**
	 * Recognize the page type based on whether the page has contentBody node or not, 
	 * text length in the whole page, and whether the informative images reside in the page. 
	 * 
	 * Based on the recognized page type, it generates surrogates.  
	 * @param domWalkInfoTagger
	 * @param contentBody
	 * @param imgNodes
	 */
	private void recognizeDocumentStructureToGenerateSurrogate(DOMWalkInformationTagger domWalkInfoTagger,
			TdNode contentBody, ArrayList<ImgElement> imgNodes) 
	{
		RecognizedDocumentStructure pageCategory = null;

		if( contentBody!=null )
		{
			// Content Pages
			pageCategory = new ContentPage(purl());
		}
		else
		{
			final int numImgNodes = imgNodes.size();
			if( (numImgNodes>0) && ((domWalkInfoTagger.getTotalTxtLength()/numImgNodes)<200) )
			{	
				// High probability to be an image-collection page
				pageCategory = new ImageCollectionPage(purl());
			}
			else if( numImgNodes!=0 )
			{
				// Index Pages (include index-content pages)
				//FIXME -- should also look at text only pages & especially use link ratio as a feature!!!!
				pageCategory = new IndexPage(purl());
			}
		}
		TreeMap<Integer, ParagraphText> paragraphTextsTMap = domWalkInfoTagger.getParagraphTextsTMap();

		if (pageCategory != null)
		{
			pageCategory.generateSurrogates(contentBody, imgNodes, domWalkInfoTagger.getTotalTxtLength(), paragraphTextsTMap, this);
		}

		// No Informative images are in this document. Form surrogate only with text.  	
		// We cannot tell whether the images in the pages are informative or not until downloading all, thus this is the case after we 
		// look through all the images in the page and determine no image is worth displaying.
		if( (numCandidatesExtractedFrom()==0) && (paragraphTextsTMap.size()>0) )
		{
			pageCategory = new TextOnlyPage(purl());
			pageCategory.generateSurrogates(contentBody, imgNodes, domWalkInfoTagger.getTotalTxtLength(), paragraphTextsTMap, this);
		}
		if (pageCategory != null)
			setRecognizedDocumentStructure(pageCategory.getClass());
	}


	/**
	 * Transform an set of AElements (HTML a) into a set of AnchorContexts.
	 * In some cases, an AElement may result in no entry, because the anchor text and anchor context are both empty.
	 * @param anchorElements
	 * @param sourcePurl The purl from which these AElements were extracted from.
	 * 
	 * @return
	 */
	public ArrayList<AnchorContext> buildAnchorContexts(ArrayList<AElement> anchorElements, ParsedURL sourcePurl, boolean fromContentBody )
	{
		ArrayList<AnchorContext> anchorNodeContexts = new ArrayList<AnchorContext>();
		
		for (AElement aElement : anchorElements)
		{
			AnchorContext aContext= constructAnchorContext(aElement, sourcePurl, fromContentBody);
			if(aContext!=null)
					anchorNodeContexts.add(aContext);
		}
		return anchorNodeContexts;
	}
	

	
	/**
	 * Given the a element from the HTML, get its anchor text (text between open and close a tags),
	 * and its anchor context (surrounding text). If either of these is not null, then return an
	 * AnchorContext object.
	 * 
	 * The surrounding text is defined as all the text in the a element's parent node.
	 * This definition should perhaps be expanded, for example, by trying grandparent if parent
	 * is either null or the same as anchor text.
	 * 
	 * @param aElement	Anchor HTMLElement (a href=...)
	 * @param fromContentBody 
	 * 
	 * @return					AnchorContext object, or null.
	 */
	public AnchorContext constructAnchorContext(AElement aElement, ParsedURL sourcePurl, boolean fromContentBody)
	{
		TdNode anchorNodeNode 				  = aElement.getNode();
		ParsedURL href 									= aElement.getHref();
		if (href != null)
		{
			//Cache TdNode-AnchorContext getTextInSubTree.
			TdNode parent 							  = anchorNodeNode.parent();
			//FIXME -- this routine drops all sorts of significant stuff because it does not concatenate across tags.
			StringBuilder anchorContext = null;
			
			if(tdNodeAnchorContextStringCache == null)
				tdNodeAnchorContextStringCache = new HashMap<TdNode, String>();

			String anchorContextString	= tdNodeAnchorContextStringCache.get(parent);
			if(anchorContextString == null)
			{
				anchorContext = getTextInSubTree(parent, false);				
				if (anchorContext != null)
				{
					anchorContextString = StringTools.unescapeAndLowerCaseStringBuilder(anchorContext);
					StringBuilderUtils.release(anchorContext);
					tdNodeAnchorContextStringCache.put(parent, anchorContextString);
				}
			}

			//TODO: provide ability to specify alternate anchorContext
			StringBuilder anchorText 			= getTextInSubTree(anchorNodeNode, true);
			if ((anchorContextString != null) || (anchorText != null))
			{

				String anchorTextString			= null;
				if (anchorText != null)
				{
					anchorTextString = StringTools.unescapeAndLowerCaseStringBuilder(anchorText);
					StringBuilderUtils.release(anchorText);
				}
				return new AnchorContext(href, anchorTextString, anchorContextString, sourcePurl, fromContentBody, false);
			}
		}
		return null;
	}


	
  public static StringBuilder getTextInSubTree(TdNode node, boolean recurse)
  {
  	return getTextinSubTree(node, recurse, null);
  }

	/**
   * Non-recursive method to get the text for the <code>node</code>
   * Collects the text even if the node contains other nodes in between,
   * specifically the <code>anchor</code>. It does not however include the 
   * text from the anchor node, as it exists in the anchorText
   * @param node
   * @param te
   * @return
   */
  public static StringBuilder getTextinSubTree(TdNode node, boolean recurse, StringBuilder result)
  {
  	for (TdNode childNode	= node.content(); childNode != null; childNode = childNode.next())
  	{
			if (recurse && (childNode.element!=null) && (!childNode.element.equals("script")))
			{
				//Recursive call with the childNode
				result = getTextinSubTree(childNode, true, result);
			}	
			else if (childNode.type == TdNode.TextNode )
  		{
  			int length	= 0;
				if (result != null)
				{
					result.append(' ');							// append space to separate text chunks
					length		= result.length();
				}
  			result			= StringBuilderUtils.trimAndDecodeUTF8(result, childNode, 0, true);
  			
  			if ((result != null) && (length == result.length()))
  					result.setLength(length - 1);	// take the space off if nothing was appended
  		} 
  		else if ("img".equals(childNode.element))
  		{
  			AttVal altAtt	= childNode.getAttrByName(ALT);
  			String alt		= (altAtt != null) ? altAtt.value : null;
  			if (!ImageFeatures.altIsBogus(alt))
  			{
  				if (result == null)
  					result		= StringBuilderUtils.acquire();
  				else
  					result.append(' ');
  				result.append(alt);
  			}
  		}
  	}
  	if (result != null)
  		XMLTools.unescapeXML(result);

  	return result;
  }
  
	public void setContent ( )
	{
		contentPage = true;		
	}

	public void setIndexPage ( )
	{
		indexPage = true;
	}

	@Override
	public boolean isIndexPage ( )
	{
		return indexPage;
	}

	@Override
	public boolean isContentPage ( )
	{
		return contentPage;
	}
	
	/**
	 * To make code navigation simpler around JTidy-parsers 
	 */
	@Override
	public void newImgTxt(ImgElement imgNode, ParsedURL anchorHref)
	{
		super.newImgTxt(imgNode, anchorHref);
	}

	@Override
	public void recycle()
	{
		this.tdNodeAnchorContextStringCache.clear();
		this.tdNodeAnchorContextStringCache = null;
		super.recycle();
	}
}