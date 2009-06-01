/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.xml.ElementState;

/**
 * @author amathur
 *
 */
public class Check extends ElementState
{

	public Check()
	{
		super();
	}
	
	/**
	 * The name of the check
	 */
	@xml_attribute private String condition;

	/**
	 * The name of the flag which this check will set.
	 */
	@xml_attribute private String name;

	/**
	 * @return the name
	 */
	public String getCondition()
	{
		return condition;
	}

	/**
	 * @param name the name to set
	 */
	public void setCondition(String name)
	{
		this.condition = name;
	}

	/**
	 * @return the flagName
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param flagName the flagName to set
	 */
	public void setName(String flagName)
	{
		this.name = flagName;
	}
}
