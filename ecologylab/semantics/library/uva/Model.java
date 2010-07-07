/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ecologylab.semantics.seeding.DocumentState;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.SIMPLTranslationException;
import ecologylab.xml.simpl_inherit;
import ecologylab.xml.library.geom.RectangularShape;

/**
 * 
 *
 * @author andruid 
 */
@simpl_inherit
public class Model extends ElementState
{
	static final String FILE_NAME = "c:/web/code/java/ecologylabFundamental/config/preferences/uvaSiteGuideExample_two_clusters_v2.xml";
	
	static final String OUT_FILE = "c:/web/code/java/ecologylabFundamental/config/preferences/uvaSiteGuideExample_parsed.xml";

	@simpl_nowrap
	@simpl_collection("topic_cluster")
	ArrayList<TopicCluster>	topicClusters;

	public ArrayList<TopicCluster> getTopicClusters() {
		if (topicClusters != null)
			return topicClusters;
		return topicClusters = new ArrayList<TopicCluster>();
	}
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
			Model model	= (Model) TranslationScope.translateFromXML(new File(FILE_NAME), getTranslations());
			
			model.serialize(System.out);
			System.out.println("\n\n");

			File outFile	= new File(OUT_FILE);
			model.serialize(outFile);
		} catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
