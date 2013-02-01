/**
 * 
 */
package ecologylab.bigsemantics.metametadata;

import java.util.HashMap;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_scalar;

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
	
	protected SemanticActionHandler	handler;
	
	public void setSemanticHandler(SemanticActionHandler semanticActionHandler)
	{
		this.handler = semanticActionHandler;
	}
	
	/**
	 * 
	 */
	public ParamOp()
	{
	}

	abstract void transformParams(HashMap<String, String> parametersMap);

}
