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
	@xml_attribute private String name;

	/**
	 * The name of the flag which this check will set.
	 */
	@xml_attribute private String flagName;

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the flagName
	 */
	public String getFlagName()
	{
		return flagName;
	}

	/**
	 * @param flagName the flagName to set
	 */
	public void setFlagName(String flagName)
	{
		this.flagName = flagName;
	}
}
