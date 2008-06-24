/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.util.ArrayList;

import ecologylab.xml.ElementState;
import ecologylab.xml.ElementState.xml_nested;
import ecologylab.xml.library.geom.Rectangle2DDoubleState;
import ecologylab.xml.library.geom.RectangularShape;

/**
 * 
 *
 * @author andruid 
 */
public class TopicCluster extends ElementState
{
	@xml_attribute	int						id;
	
	@xml_nested		Rectangle2DDoubleState		rectangle;
	
	@xml_nested		DocumentSet				documentSet;
	
	@xml_collection("keyword_set")		
					ArrayList<KeywordSet>	keywordSet;
	
	/**
	 * 
	 */
	public TopicCluster()
	{
		// TODO Auto-generated constructor stub
	}

}
