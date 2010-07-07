/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.util.ArrayList;

import ecologylab.xml.ElementState;
import ecologylab.xml.ElementState.simpl_composite;
import ecologylab.xml.ElementState.simpl_nowrap;
import ecologylab.xml.library.geom.Rectangle2DDoubleState;
import ecologylab.xml.library.geom.RectangularShape;

/**
 * 
 *
 * @author andruid 
 */
public class TopicCluster extends ElementState
{
	@simpl_scalar	int						id;
	
	@simpl_composite		Rectangle2DDoubleState		rectangle;
	
	@simpl_composite		DocumentSet				documentSet;
	
	@simpl_nowrap 
	@simpl_collection("keyword_set")		
					ArrayList<KeywordSet>	keywordSets;
	
	/**
	 * 
	 */
	public TopicCluster()
	{
		// TODO Auto-generated constructor stub
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public Rectangle2DDoubleState getRectangle()
	{
		return rectangle;
	}

	public void setRectangle(Rectangle2DDoubleState rectangle)
	{
		this.rectangle = rectangle;
	}

	public DocumentSet getDocumentSet()
	{
		return documentSet;
	}

	public void setDocumentSet(DocumentSet documentSet)
	{
		this.documentSet = documentSet;
	}

	public ArrayList<KeywordSet> getKeywordSets()
	{
		return keywordSets;
	}

	public void setKeywordSets(ArrayList<KeywordSet> keywordSet)
	{
		this.keywordSets = keywordSet;
	}

	
}
