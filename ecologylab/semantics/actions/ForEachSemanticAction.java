/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 * 
 */
@xml_inherit
public @xml_tag(SemanticActionStandardMethods.FOR_EACH)
class ForEachSemanticAction extends NestedSemanticAction implements SemanticActionStandardMethods
{

	/**
	 * Name of the collection object on which for loop is applied
	 */
	@xml_attribute private String collection;
	
	/**
	 * Name of the iterator [iterator variable]
	 */
	@xml_attribute private String as;
	
	/**
	 * Starting index
	 */
	@xml_attribute private String start;
	
	/**
	 * end index
	 */
	@xml_attribute private String end;
	
	/**
	 * current index of  loop
	 */
	@xml_attribute private String currentIndex;
	
	
	/**
	 *  variable to store collection size.
	 */
 @xml_attribute private String  size;
	
	@Override
	public String getActionName()
	{
		return FOR_EACH;
	}

	/**
	 * @return the collection
	 */
	public String getCollection()
	{
		return collection;
	}

	/**
	 * @param collection the collection to set
	 */
	public void setCollection(String collection)
	{
		this.collection = collection;
	}

	/**
	 * @return the as
	 */
	public String getAs()
	{
		return as;
	}

	/**
	 * @param as the as to set
	 */
	public void setAs(String as)
	{
		this.as = as;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the start
	 */
	public String getStart()
	{
		return start;
	}

	/**
	 * @return the end
	 */
	public String getEnd()
	{
		return end;
	}

	/**
	 * @return the curIndex
	 */
	public String getCurIndex()
	{
		return currentIndex;
	}

	/**
	 * @param curIndex the curIndex to set
	 */
	public void setCurIndex(String curIndex)
	{
		this.currentIndex = curIndex;
	}

	/**
	 * @return the size
	 */
	public String getSize()
	{
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(String size)
	{
		this.size = size;
	}



}
