package ecologylab.media.html.dom;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.tidy.DOMDocumentImpl;
import org.w3c.tidy.Lexer;
import org.w3c.tidy.Out;
import org.w3c.tidy.OutImpl;
import org.w3c.tidy.StreamIn;
import org.w3c.tidy.TdNode;
import org.w3c.tidy.Tidy;

import ecologylab.generic.StringBuilderPool;
import ecologylab.generic.StringTools;
import ecologylab.media.html.dom.documentstructure.AnchorContext;
import ecologylab.media.html.dom.documentstructure.ContentPage;
import ecologylab.media.html.dom.documentstructure.ImageCollectionPage;
import ecologylab.media.html.dom.documentstructure.IndexPage;
import ecologylab.media.html.dom.documentstructure.TextOnlyPage;


/**
 * Connect to JTidy parser to parse HTML pages for cF.
 * Bridge code between combinFormation and JTidy
 * This parsing code integrates with the Image-Text Surrogate extractor code.
 * 
 * @author eunyee
 *
 */
public class HTMLDOMParser extends Tidy
{

	public static final String textContext		= "TEXT_CONTEXT";
	public static final String TextNode			= "TextNode";
	public static final String ImgNode			= "ImageNode";
	public static final String extractedCaption 	= "EXTRACTED_CAPTION";

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
	 * @param out
	 * @param htmlType
	 */
	public org.w3c.dom.Document parse(InputStream in)
	{
		org.w3c.dom.Document doc = parseDOM(in, null);


		return doc;
	}

	String url = "";

	/**
	 * Extract Image and Text surrogates while walk through DOM 
	 * 
	 * @param in
	 * @param htmlType
	 */
	public void parse(InputStream in, TidyInterface htmlType, String url)
	{
		this.url = url;
		Document parsedDoc = parseDOM(in, null);

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
  	
		taggedDoc.paragraphTexts.clear();
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
		ArrayList<HtmlNodewithAttr> imgNodes = taggedDoc.getAllImgNodes();

		recognizeDocumentStructureToGenerateSurrogate(htmlType, taggedDoc, contentBody, imgNodes);
	}

	/**
	 * This is the walk of the dom that calls print tree, and the parser methods such as closeHref etc.
	 * @param doc
	 * @param htmlType
	 * @return
	 */
	private DOMWalkInformationTagger walkAndTagDom(org.w3c.dom.Document doc, TidyInterface htmlType)
	{
		Out jtidyPrettyOutput = new OutImpl();
		TdNode rootTdNode;

		rootTdNode = ((DOMDocumentImpl)doc).adaptee;

		jtidyPrettyOutput.state = StreamIn.FSM_ASCII;
		jtidyPrettyOutput.encoding = configuration.CharEncoding;

		DOMWalkInformationTagger domTagger = new DOMWalkInformationTagger(configuration, htmlType);
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
	 * @param pprint
	 * @param contentBody
	 * @param imgNodes
	 */
	private void recognizeDocumentStructureToGenerateSurrogate(TidyInterface htmlType,
			DOMWalkInformationTagger pprint, TdNode contentBody,
			ArrayList<HtmlNodewithAttr> imgNodes) 
	{
		RecognizedDocumentStructure pageCategory = null;

		if( contentBody!=null )
		{
			// Content Pages
			pageCategory = new ContentPage();
		}
		else if( imgNodes.size()==0 ) //) && (pprint.getTotalTxtLength()<200) )
		{
			pageCategory = new TextOnlyPage();
		}   	
		else if( (imgNodes.size()>0) && ((pprint.getTotalTxtLength()/imgNodes.size())<200) )
		{	
			// High probability to be an image-collection page
			pageCategory = new ImageCollectionPage();
		}
		else
		{
			// Index Pages (include index-content pages)
			pageCategory = new IndexPage();
		}

		pageCategory.generateSurrogates(contentBody, imgNodes, pprint.getTotalTxtLength(), pprint.paragraphTexts, htmlType);


		// No Informative images are in this document. Form surrogate only with text.  	
		// We cannot tell whether the images in the pages are informative or not until downloding all, thus this is the case after we 
		// look through all the images in the page and determine no image is worth displaying.
		if( (htmlType.numSurrogateFrom()==0) && (pprint.paragraphTexts.size()>0) )
		{
			pageCategory = new TextOnlyPage();
			pageCategory.generateSurrogates(contentBody, imgNodes, pprint.getTotalTxtLength(), pprint.paragraphTexts, htmlType);
		}

	}

	/**
	 * @param arrayList 
	 * @param articleMain
	 */
	protected ArrayList<AnchorContext> findHrefsAndContext(TidyInterface htmlType, ArrayList<HtmlNodewithAttr> anchorNodes)
	{
		ArrayList<AnchorContext> anchorNodeContexts = new ArrayList<AnchorContext>();
		for(HtmlNodewithAttr anchorNode : anchorNodes)
		{
			TdNode node 									= anchorNode.getNode().parent();
			String anchorContextStr 			= getTextinSubTree(node);
			String anchorText 						= getTextinSubTree(anchorNode.getNode());
			String href 									= (String) anchorNode.getAttributesMap().get("href");
			if(href == null)
				continue;
			anchorNodeContexts.add(new AnchorContext(href,anchorText,anchorContextStr));

		}
		return anchorNodeContexts;
	}

	/**
	 * string builder pool to parse link metadata
	 */
	protected static final StringBuilderPool stringBuilderPool = new StringBuilderPool(10, 512);
	
  /**
   * Non-recursive method to get the text for the <code>node</code>
   * Collects the text even if the node contains other nodes in between,
   * specifically the <code>anchor</code>. It does not however include the 
   * text from the anchor node.
   * @param node
   * @param te
   * @return
   */
  public String getTextinSubTree(TdNode node)
  {
  	TdNode childNode	= node.content();
  	StringBuilder buffy = stringBuilderPool.acquire();
  	
  	while( childNode != null )
  	{
  		if( childNode.type == TdNode.TextNode )
  		{
  			String tempstr = Lexer.getString(childNode.textarray(), childNode.start(), childNode.end()-childNode.start());
  			tempstr = tempstr.trim();
  			if(!tempstr.startsWith("<!--")) 
  				buffy.append(" ").append(tempstr);	//+= allows collecting text across nodes within the current node
  		}
  		childNode = childNode.next();
  	}
  	String textInSubTree = StringTools.toString(buffy);
  	stringBuilderPool.release(buffy);
	
  	return textInSubTree;
  }
}