package ecologylab.media.html.dom;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.w3c.tidy.DOMDocumentImpl;
import org.w3c.tidy.Out;
import org.w3c.tidy.OutImpl;
import org.w3c.tidy.StreamIn;
import org.w3c.tidy.TdNode;
import org.w3c.tidy.Tidy;

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
		extractImageTextSurrogates(parseDOM(in, null), htmlType); 
	}

	/**
	 * Extract Image and Text Surrogates while walk through DOM
	 * 
	 * historically was called as pprint() in JTidy. 
	 */
	public void extractImageTextSurrogates(org.w3c.dom.Document doc, TidyInterface htmlType)
	{
		Out jtidyPrettyOutput = new OutImpl();
		TdNode rootTdNode;

		if (!(doc instanceof DOMDocumentImpl)) {
			return;
		}
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

		TdNode contentBody = RecognizedDocumentStructure.recognizeContentBody(domTagger);
		//System.out.println("\n\ncontentBody = " + contentBody);       
		ArrayList<ImgNodewithAttr> imgNodes = domTagger.getAllImgNodes();

		recognizeDocumentStructureToGenerateSurrogate(htmlType, domTagger, contentBody, imgNodes);

		domTagger.paragraphTexts.clear();

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
			ArrayList<ImgNodewithAttr> imgNodes) 
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



}