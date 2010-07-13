/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * @author amathur TODO Flag checks can latter have nested semantic actions
 * 
 */

@simpl_inherit
@xml_tag("flag_check")
public class FlagCheck extends FlagCheckBase
{

	public FlagCheck()
	{
		super();
	}

	/**
	 * The name of the flags to be checked
	 */
	@simpl_scalar
	private String	value;

	/**
	 * @return the name
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setValue(String name)
	{
		this.value = name;
	}

	@Override
	public boolean evaluate(SemanticActionHandler handler)
	{
		return (Boolean) handler.getSemanticActionReturnValueMap().get(getValue());
	}
	
}
