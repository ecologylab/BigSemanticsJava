/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.HashMap;
import java.util.List;

import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

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
	
	@simpl_scalar
	private String			valueFrom;
	
	@simpl_scalar
	private String			valueFromCollection;
	
	@simpl_scalar
	private String			collectionIndex;
	
	/**
	 * 
	 */
	public SetParam()
	{
	}


	@Override
	void transformParams(HashMap<String, String> parametersMap)
	{
		if (value != null)
			parametersMap.put(getName(), value);
		else if (valueFrom != null && handler != null)
		{
			parametersMap.put(getName(), handler.getSemanticActionVariableMap().get(valueFrom).toString());
		}
		else if (valueFromCollection != null && collectionIndex != null && handler != null)
		{
			Object idx = handler.getSemanticActionVariableMap().get(collectionIndex);
			if (idx instanceof Integer)
			{
				int i = (Integer) idx;
				List theCollection = (List) handler.getSemanticActionVariableMap().get(valueFromCollection);
				if (i >= 0 && i < theCollection.size())
					parametersMap.put(getName(), theCollection.get(i).toString());
			}
		}
	}

}
