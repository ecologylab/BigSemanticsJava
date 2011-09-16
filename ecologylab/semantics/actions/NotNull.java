package ecologylab.semantics.actions;

import ecologylab.collections.Scope;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class NotNull extends Condition
{

	/**
	 * This is the name of the element you want to check whether it is null or not.
	 */
	@simpl_scalar
	private String	value;

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * If the element is found in the semantic action environment and is not a null pointer, return
	 * true. Otherwise, return false.
	 */
	@Override
	public boolean evaluate(SemanticActionHandler handler)
	{
		String name = getValue();
		Scope theMap = handler.getSemanticActionVariableMap();

		return theMap.containsKey(name) && theMap.get(name) != null;
	}

}
