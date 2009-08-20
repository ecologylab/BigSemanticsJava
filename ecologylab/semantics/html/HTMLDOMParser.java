package ecologylab.semantics.html;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.tidy.AttVal;
import org.w3c.tidy.DOMDocumentImpl;
import org.w3c.tidy.DOMNodeImpl;
import org.w3c.tidy.Lexer;
import org.w3c.tidy.Out;
import org.w3c.tidy.OutImpl;
import org.w3c.tidy.StreamIn;
import org.w3c.tidy.TdNode;
import org.w3c.tidy.Tidy;

import sun.io.ByteToCharASCII;

import ecologylab.generic.StringTools;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.semantics.html.documentstructure.ContentPage;
import ecologylab.semantics.html.documentstructure.ImageCollectionPage;
import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.html.documentstructure.IndexPage;
import ecologylab.semantics.html.documentstructure.TextOnlyPage;
import ecologylab.semantics.html.utils.HTMLAttributeNames;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.xml.XMLTools;


/**
 * Connect to JTidy parser to parse HTML pages for cF.
 * Bridge code between combinFormation and JTidy
 * This parsing code integrates with the Image-Text Surrogate extractor code.
 * 
 * @author eunyee
 *
 */
public class HTMLDOMParser extends Tidy
implements HTMLAttributeNames
{
	PURLConnection purlConnection;

	/**
	 * because Tidy extends Serializable
	 */
	private static final long serialVersionUID = 1L;

	public HTMLDOMParser()
	{	
		super();
	}

	/**
	 * Parse HTML Document, and return the root DOM node
	 * 
	 * @param in
	 * @param purl TODO
	 * @param out
	 * @param htmlType
	 */
	public org.w3c.dom.Document parse(PURLConnection purlConnection)
	{
		this.purlConnection		= purlConnection;
		return parseDOM(purlConnection.inputStream(), null);
	}

	/**
	 * Extract Image and Text surrogates while walk through DOM 
	 * 
	 * @param in
	 * @param htmlType
	 */
	public void parse(PURLConnection purlConnection, TidyInterface htmlType)
	{
		Document parsedDoc = parse(purlConnection);

		if (!(parsedDoc instanceof DOMDocumentImpl)) 
		{
			//Does this ever happen anymore?
			return;
		}
		
		DOMWalkInformationTagger taggedDoc = walkAndTagDom(parsedDoc, htmlType);
		
		extractImageTextSurrogates(taggedDoc, htmlType);
		
		//Now, find hrefs, with their context and generate containers with metadata
		ArrayList<AnchorContext> anchorContexts = findHrefsAndContext(htmlType, taggedDoc.getAllAnchorNodes());
		
  	if(htmlType != null)
			htmlType.generateContainersFromContexts(anchorContexts);
  	
  	anchorContexts.clear();
		taggedDoc.recycle();
	}

	/**
	 * Extract Image and Text Surrogates while walk through DOM
	 * 
	 * historically was called as pprint() in JTidy. 
	 */
	public void extractImageTextSurrogates(DOMWalkInformationTagger taggedDoc, TidyInterface htmlType)
	{

		TdNode contentBody = RecognizedDocumentStructure.recognizeContentBody(taggedDoc);
		//System.out.println("\n\ncontentBody = " + contentBody);       
		ArrayList<ImgElement> imgNodes = taggedDoc.getAllImgNodes();

		recognizeDocumentStructureToGenerateSurrogate(htmlType, taggedDoc, contentBody, imgNodes);
	}
	public DOMWalkInformationTagger walkAndTagDom(org.w3c.dom.Node doc, TidyInterface htmlType)
	{
		return walkAndTagDom(((DOMNodeImpl)doc).adaptee, htmlType);
	}
	/**
	 * This is the walk of the dom that calls print tree, and the parser methods such as closeHref etc.
	 * @param doc
	 * @param htmlType
	 * @return
	 */
	public DOMWalkInformationTagger walkAndTagDom(TdNode rootTdNode, TidyInterface htmlType)
	{
		Out jtidyPrettyOutput = new OutImpl();

		jtidyPrettyOutput.state = StreamIn.FSM_ASCII;
		jtidyPrettyOutput.encoding = configuration.CharEncoding;

		DOMWalkInformationTagger domTagger = new DOMWalkInformationTagger(configuration, purlConnection.getPurl(), htmlType);
		domTagger.state = StreamIn.FSM_ASCII;
		domTagger.encoding = configuration.CharEncoding;

		// walk through the HTML document object.
		// gather all paragraphText and image objects in the data structure.
		//FIXME -- get rid of this call and object!
		domTagger.printTree(jtidyPrettyOutput, (short)0, 0, null, rootTdNode);

		domTagger.flushLine(jtidyPrettyOutput, 0);
		return domTagger;
	}

	/**
	 * Recognize the page type based on whether the page has contentBody node or not, 
	 * text length in the whole page, and whether the informative images reside in the page. 
	 * 
	 * Based on the recognized page type, it generates surrogates.  
	 * 
	 * @param htmlType
	 * @param domWalkInfoTagger
	 * @param contentBody
	 * @param imgNodes
	 */
	private void recognizeDocumentStructureToGenerateSurrogate(TidyInterface htmlType,
			DOMWalkInformationTagger domWalkInfoTagger, TdNode contentBody,
			ArrayList<ImgElement> imgNodes) 
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
			pageCategory.generateSurrogates(contentBody, imgNodes, domWalkInfoTagger.getTotalTxtLength(), paragraphTextsTMap, htmlType);
		}

		// No Informative images are in this document. Form surrogate only with text.  	
		// We cannot tell whether the images in the pages are informative or not until downloding all, thus this is the case after we 
		// look through all the images in the page and determine no image is worth displaying.
		if( (htmlType.numCandidatesExtractedFrom()==0) && (paragraphTextsTMap.size()>0) )
		{
			pageCategory = new TextOnlyPage(purl());
			pageCategory.generateSurrogates(contentBody, imgNodes, domWalkInfoTagger.getTotalTxtLength(), paragraphTextsTMap, htmlType);
		}
		if (pageCategory != null)
			htmlType.setRecognizedDocumentStructure(pageCategory.getClass());
	}

	/**
	 * @param arrayList 
	 * @param articleMain
	 */
	public ArrayList<AnchorContext> findHrefsAndContext(TidyInterface htmlType, ArrayList<AElement> anchorElements)
	{
		ArrayList<AnchorContext> anchorNodeContexts = new ArrayList<AnchorContext>();
		
		for (AElement aElement : anchorElements)
		{
			AnchorContext aContext= findHrefsAndContext(htmlType, aElement);
			if(aContext!=null)
			{
					anchorNodeContexts.add(aContext);
			}
				
		}
		return anchorNodeContexts;
	}
	
	public AnchorContext findHrefsAndContext(TidyInterface htmlType,AElement aElement)
	{
		TdNode anchorNodeNode 				  = aElement.getNode();
		ParsedURL href 									= aElement.getHref();
		if (href != null)
		{
			TdNode parent 							  = anchorNodeNode.parent();
			//FIXME -- this routine drops all sorts of significant stuff because it does not concatenate across tags.
			StringBuilder anchorContext 	= getTextInSubTree(parent, false);
			
			//TODO: provide abilty to specify alternate anchorContext
			StringBuilder anchorText 			= getTextInSubTree(anchorNodeNode, true);
			if ((anchorContext != null) || (anchorText != null))
			{
				String anchorContextString	= null;
				if (anchorContext != null)
				{
					XMLTools.unescapeXML(anchorContext);
					StringTools.toLowerCase(anchorContext);
					anchorContextString				= StringTools.toString(anchorContext);
					StringBuilderUtils.release(anchorContext);
				}
				String anchorTextString			= null;
				if (anchorText != null)
				{
					XMLTools.unescapeXML(anchorText);
					StringTools.toLowerCase(anchorText);
					anchorTextString					= StringTools.toString(anchorText);
					StringBuilderUtils.release(anchorText);
				}
				return new AnchorContext(href, anchorTextString, anchorContextString);
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
   * text from the anchor node.
   * @param node
   * @param te
   * @return
   */
	//FIXME -- why is text in anchor node not included?
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
  
  ParsedURL purl()
  {
  	return purlConnection.getPurl();
  }
}