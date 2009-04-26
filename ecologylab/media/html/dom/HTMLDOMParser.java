package ecologylab.media.html.dom;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.tidy.DOMDocumentImpl;
import org.w3c.tidy.Lexer;
import org.w3c.tidy.Out;
import org.w3c.tidy.OutImpl;
import org.w3c.tidy.StreamIn;
import org.w3c.tidy.TdNode;
import org.w3c.tidy.Tidy;

import sun.io.ByteToCharASCII;

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
	String url 			= "";

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
		return parseDOM(in, null);
	}

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
	 * @param domWalkInfoTagger
	 * @param contentBody
	 * @param imgNodes
	 */
	private void recognizeDocumentStructureToGenerateSurrogate(TidyInterface htmlType,
			DOMWalkInformationTagger domWalkInfoTagger, TdNode contentBody,
			ArrayList<HtmlNodewithAttr> imgNodes) 
	{
		RecognizedDocumentStructure pageCategory = null;

		if( contentBody!=null )
		{
			// Content Pages
			pageCategory = new ContentPage();
		}
		else
		{
			final int numImgNodes = imgNodes.size();
			if( (numImgNodes>0) && ((domWalkInfoTagger.getTotalTxtLength()/numImgNodes)<200) )
			{	
				// High probability to be an image-collection page
				pageCategory = new ImageCollectionPage();
			}
			else if( numImgNodes!=0 )
			{
				// Index Pages (include index-content pages)
				//FIXME -- should also look at text only pages & especially use link ratio as a feature!!!!
				pageCategory = new IndexPage();
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
			pageCategory = new TextOnlyPage();
			pageCategory.generateSurrogates(contentBody, imgNodes, domWalkInfoTagger.getTotalTxtLength(), paragraphTextsTMap, htmlType);
		}

	}

	/**
	 * @param arrayList 
	 * @param articleMain
	 */
	protected ArrayList<AnchorContext> findHrefsAndContext(TidyInterface htmlType, ArrayList<HtmlNodewithAttr> anchorNodes)
	{
		ArrayList<AnchorContext> anchorNodeContexts = new ArrayList<AnchorContext>();
		//System.out.println("\n\n------------Parsing " + anchorNodes.size()+"\n-----------------");
		
		for(HtmlNodewithAttr anchorNode : anchorNodes)
		{
			TdNode node 									= anchorNode.getNode().parent();
			String anchorContextStr 			= getTextinSubTree(node);
			String anchorText 						= getTextinSubTree(anchorNode.getNode());
			String href 									= anchorNode.getAttribute("href");
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
  	
  	StringBuilder buffy 	= stringBuilderPool.acquire();
  	boolean first					= true;
  	for (TdNode childNode	= node.content(); childNode != null; childNode = childNode.next())
  	{
  		if (childNode.type == TdNode.TextNode )
  		{
  			byte[] textarray	= childNode.textarray();

  			int start				 	= childNode.start();
  			int end 					= childNode.end();
  			// trim in place				
  			while (Character.isWhitespace((char) textarray[start]) && (start < end))
  			{
  				start++;
  			}
  			while (Character.isWhitespace((char) textarray[end - 1]) && (start < end))
  			{
  				end--;
  			}
  			int length				= end-start;

  			if((length >0) && !((length >= 4) && (textarray[0] == '<') &&
  					(textarray[1] == '!') && (textarray[2] == '-') && (textarray[3] == '-')))
  			{
  				//FIXME -- use CharBuffer or StringBuilder here!
  				String tempstr	= Lexer.getString(textarray, start, end-start);
  				if (!first)
  					buffy.append(' ');
  				else
  					first					= false;

  				buffy.append(tempstr);	//+= allows collecting text across nodes within the current node
  			}
  		}
  	}
  	String textInSubTree = StringTools.toString(buffy);
  	stringBuilderPool.release(buffy);

  	return textInSubTree;
  }
}