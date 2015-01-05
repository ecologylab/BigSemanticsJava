/**
 * 
 */
package ecologylab.bigsemantics.metadata;

import ecologylab.bigsemantics.generated.library.Channel;
import ecologylab.bigsemantics.generated.library.Rss;
import ecologylab.bigsemantics.generated.library.Item;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.serialization.library.rss.RssState;
import ecologylab.serialization.library.rss.RssTranslations;

/**
 * @author andruid
 *
 */
public class RssTest
{
	public static final ParsedURL CNN_TOP_FEED	= ParsedURL.getAbsolute("http://rss.cnn.com/rss/cnn_topstories.rss");
  private static final String TRANSLATION_SPACE_NAME	= "rss_test";
  
  public static SimplTypesScope get()
  {
	   return SimplTypesScope.get(TRANSLATION_SPACE_NAME, RssState.class, Channel.class, Item.class);
  }


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ElementState rss;
		try
		{
//		rss = translateFromXMLCharSequence(FLICKR_EXAMPLE, RssTranslations.get());
//			rss = translateFromXMLCharSequence(NABEEL_TEST, RssTranslations.get());
			rss = (ElementState) RssTranslations.get().deserialize(CNN_TOP_FEED, Format.XML);
			
			System.out.println("");			
			SimplTypesScope.serialize(rss, System.out, StringFormat.XML);
			System.out.println("");
			
			// RssTranslations.get().translateToXML(System.out);

		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
