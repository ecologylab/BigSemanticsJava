/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.HashMap;

import ecologylab.serialization.simpl_inherit;

/**
 * Operation specifies adding param with this name and value to the query string for the location ParsedURL.
 * 
 * @author andruid
 */
@simpl_inherit
public class SetParam extends ParamOp
{
	@simpl_scalar
	private String			value;
	
	
	/**
	 * 
	 */
	public SetParam()
	{
	}


	@Override
	void transformParams(HashMap<String, String> parametersMap)
	{
		parametersMap.put(getName(), value);
	}

}
