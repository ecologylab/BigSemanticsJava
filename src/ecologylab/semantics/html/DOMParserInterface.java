package ecologylab.semantics.html;

import java.util.ArrayList;

import org.w3c.dom.Node;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.documentstructure.AnchorContext;
import ecologylab.semantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.semantics.metadata.builtins.ImageClipping;


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