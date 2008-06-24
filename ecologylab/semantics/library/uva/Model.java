/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.io.File;
import java.io.IOException;

import ecologylab.services.messages.cf.DocumentState;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.library.geom.RectangularShape;
import ecologylab.xml.types.element.ArrayListState;

/**
 * 
 *
 * @author andruid 
 */
@xml_inherit
public class Model extends ArrayListState<TopicCluster>
{
	static final String FILE_NAME = "c:/web/code/java/ecologylabFundamental/config/preferences/siteGuideExample_two_clusters_v2.xml";
	
	static final String OUT_FILE = "c:/web/code/java/ecologylabFundamental/config/preferences/siteGuideExample_parsed.xml";


	public static TranslationScope getTranslations()
	{
		return TranslationScope.get("uva_site_guide", Model.class, DocumentSet.class, KeywordSet.class, TopicCluster.class, DocumentState.class, RectangularShape.class);
	}
	/**
	 * 
	 */
	public Model()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			Model model	= (Model) ElementState.translateFromXML(new File(FILE_NAME), getTranslations());
			
			model.translateToXML(System.out);
			System.out.println("\n\n");

			File outFile	= new File(OUT_FILE);
			model.translateToXML(outFile);
		} catch (XMLTranslationException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
