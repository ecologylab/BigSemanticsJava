/**
 * 
 */
package ecologylab.semantics.connectors;

/**
 * @author andruid
 *
 */
public interface SearchEngineNames
{
  static final String  SEARCH		= "search";
  
  static final String  QUERY		= "query";
  
  // engine names
  public static final String	GOOGLE		= "google";
  public static final String	FLICKR		= "flickr";
  public static final String	YAHOO		= "yahoo";
  public static final String	YAHOO_IMAGE	= "yahoo_image";
  public static final String	YAHOO_NEWS	= "yahoo_news";
  public static final String	YAHOO_BUZZ	= "yahoo_buzz";
  public static final String	DELICIOUS	= "delicious";
  public static final String	ICDL		= "icdl";
  public static final String	NSDL		= "nsdl";
  public static final String	CITESEER	= "citeseer";
  public static final String	ACMPORTAL	= "acm_portal";
  
  //constants for Google Search [Might be used in other searches also]
  //--------------------------------------------------------
	public static final int	REGULAR		= 0;
	public static final int	RELATED		= 1;
	public static final int	IMAGE		= 2;
	public static final int	SITE		= 3;   
	
	static final String GOOGLE_NO_PDF_ARG	= "+-filetype%3Apdf";

	static final String regularGoogleSearchURLString
	= "http://www.google.com/search?q=";

	static final String relatedGoogleSearchUrlString
	= "http://www.google.com/search?q=related:";

	static final String imageGoogleSearchURLString
	= "http://images.google.com/images?safe=off&q=";
	
	//--------------------------------------------------------------------
  

}
