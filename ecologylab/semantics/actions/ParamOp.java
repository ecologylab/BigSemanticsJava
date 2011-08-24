/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.HashMap;

import ecologylab.serialization.ElementState;

/**
 * @author andruid
 *
 */
abstract
public class ParamOp extends ElementState
{
	@simpl_scalar
	private String			name;
	
	public String getName()
	{
		return name;
	}

	/**
	 * 
	 */
	public ParamOp()
	{
	}

	abstract void transformParams(HashMap<String, String> parametersMap);
}