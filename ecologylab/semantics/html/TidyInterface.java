package ecologylab.semantics.html;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.tidy.TdNode;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.documentstructure.AnchorContext;


/**
 * JTidy (DOM) parser interface. 
 * 
 * @author eunyee
 *
 */
public interface TidyInterface
{
	public void setTitle(TdNode node);
	
	public void setBold(boolean on) ;
	
	public void setItalic(boolean on);
	
	public void newAHref(HashMap<String, String> attributesMap);

	public void generateContainersFromContexts(ArrayList<AnchorContext> anchorContexts);
	
	public void closeHref();
	
	public void newImgTxt(HTMLElement htmlNode, ParsedURL anchorHref);
	
	//TODO -- take a BtringBuilder instead of a String. and use if efficiently!!!!!!
	public void newTxt(StringBuilder surrogateText);
	
	public int numCandidatesExtractedFrom();
	
	public void removeTheContainerFromCandidates(ParsedURL containerPURL);
	
	public ParsedURL getAnchorParsedURL(String anchorURL);
	
	/**
	 * a surrogate for the other container.
	 * 
	 * @param attributesMap
	 * @param anchorHref
	 */
	public void newAnchorImgTxt(HTMLElement htmlNode, ParsedURL anchorHref);

	public void setIndexPage ( );

	public void setContent ( );
	
	public void setRecognizedDocumentStructure(Class<? extends RecognizedDocumentStructure> pageType);
	
//	public void setDocument(org.w3c.dom.Document node);
}