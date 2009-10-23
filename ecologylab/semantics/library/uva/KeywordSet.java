/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.util.ArrayList;

import ecologylab.xml.ElementState;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_nowrap;
import ecologylab.xml.types.element.ArrayListState;

/**
 * 
 *
 * @author andruid 
 */
@xml_inherit
public class KeywordSet extends ElementState
{
	@xml_attribute	String	type;
	
	@xml_nowrap 
	@xml_collection("keyword")	ArrayList<String>	keywords;
	
	
	/**
	 * 
	 */
	public KeywordSet()
	{
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(Object e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, Object element)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public Object set(int index, Object element)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public ArrayList<String> getKeywords()
	{
		return keywords;
	}

	public void setKeywords(ArrayList<String> keywords)
	{
		this.keywords = keywords;
	}

}
