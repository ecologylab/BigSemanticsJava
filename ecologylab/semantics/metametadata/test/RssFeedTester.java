/**
 * 
 */
package ecologylab.semantics.metametadata.test;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 *
 */
public class RssFeedTester extends SemanticsTest
{

	/**
	 * @throws SIMPLTranslationException
	 */
	public RssFeedTester() throws SIMPLTranslationException
	{
		// TODO Auto-generated constructor stub
	}
	public static final ParsedURL NYT_TECH_FEED	= ParsedURL.getAbsolute("http://www.nytimes.com/services/xml/rss/nyt/Technology.xml");
	public static final ParsedURL CNN_TOP_FEED	= ParsedURL.getAbsolute("http://rss.cnn.com/rss/cnn_topstories.rss");
	
	public static final ParsedURL BBC_FRONT_FEED	= ParsedURL.getAbsolute("http://news.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml");
	
	public static final ParsedURL ABC_SPORTS_FEED	= ParsedURL.getAbsolute("http://my.abcnews.go.com/rsspublic/sports_rss20.xml");
	
	public static final ParsedURL FLICKR_FEED	= ParsedURL.getAbsolute("http://www.flickr.com/services/feeds/photos_public.gne?format=rss_200&tags=sunset");
	
	public static final ParsedURL DELICIOUS_FEED	= ParsedURL.getAbsolute("http://del.icio.us/rss/andruid/");

	
	
	public static final String TEST1 = "<image mm_name=\"image\" caption=\"Summer field in Belgium (Hamois). The blue flower is Centaurea cyanus and the red one a Papaver rhoeas.\" location=\"http://upload.wikimedia.org/wikipedia/commons/thumb/c/c4/Field_Hamois_Belgium_Luc_Viatour.jpg/250px-Field_Hamois_Belgium_Luc_Viatour.jpg\"></image>";

  public static void main(String[] args)
  { 	
		try
		{
	  	new MetadataSerializationTest();

	  	final TranslationScope T_SCOPE	= GeneratedMetadataTranslationScope.get();

	  	ElementState feed	= T_SCOPE.deserialize(NYT_TECH_FEED);
			
			feed.serialize(System.out);
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
}
