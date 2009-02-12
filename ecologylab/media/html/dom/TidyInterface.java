package ecologylab.media.html.dom;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.tidy.TdNode;

import ecologylab.media.html.dom.documentstructure.AnchorContext;
import ecologylab.net.ParsedURL;


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

	public void newAHref(ArrayList<AnchorContext> anchorContexts);
	
	public void closeHref();
	
	public void newImgTxt(HashMap attributesMap, ParsedURL anchorHref);
	
	//TODO -- take a BtringBuilder instead of a String. and use if efficiently!!!!!!
	public void newTxt(String surrogateText);
	
	public int numSurrogateFrom();
	
	public void removeTheContainerFromCandidates(ParsedURL containerPURL);
	
	public ParsedURL getAnchorParsedURL(String anchorURL);
	
	/**
	 * a surrogate for the other container.
	 * 
	 * @param attributesMap
	 * @param anchorHref
	 */
	public void newAnchorImgTxt(HashMap attributesMap, ParsedURL anchorHref);

	public void setIndexPage ( );

	public void setContent ( );
	
//	public void setDocument(org.w3c.dom.Document node);
}