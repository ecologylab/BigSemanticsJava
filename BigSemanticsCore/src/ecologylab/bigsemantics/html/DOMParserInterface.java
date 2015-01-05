package ecologylab.bigsemantics.html;

import java.util.ArrayList;

import org.w3c.dom.Node;

import ecologylab.bigsemantics.html.documentstructure.AnchorContext;
import ecologylab.bigsemantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.bigsemantics.metadata.builtins.ImageClipping;
import ecologylab.net.ParsedURL;


/**
 * Interface for connecting our DOMImageText extraction code to various DOM Parsers, starting w Tidy.
 * 
 * @author eunyee, aaron, andruid
 */
public interface DOMParserInterface
{
	public void setTitle(Node node);
	
	public void setBold(boolean on) ;
	
	public void setItalic(boolean on);

	public void generateCandidateContainersFromContexts(ArrayList<AnchorContext> anchorContexts, boolean fromContentBody);
	
	/**
	 * Construct a TextClipping, with text not associated with an ImageClipping.
	 * Associate with this.
	 * 
	 * @param paraText
	 */
	public void constructTextClipping(ParagraphText paraText);
	
	public int numExtractedClippings();
	
	public void removeTheContainerFromCandidates(ParsedURL containerPURL);
	
/**
 * Construct a clipping for a different Document.
 * 
 * @param imgNode
 * @param anchorHref
 * @return
 */
	public ImageClipping constructAnchorImageClipping(ImgElement imgNode, ParsedURL anchorHref);
	
	public ImageClipping constructImageClipping(ImgElement imgNode, ParsedURL anchorHref);

	public void setIndexPage ( );

	public void setContent ( );
	
	public void setRecognizedDocumentStructure(Class<? extends RecognizedDocumentStructure> pageType);
	
//	public void setDocument(org.w3c.dom.Document node);
}