/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.HashMap;

/**
 * Operation specifies removing the param with this name from the location ParsedURL.
 * 
 * @author andruid
 */
public class StripParam extends ParamOp
{

	/**
	 * 
	 */
	public StripParam()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * Remove the parameter of the name from this from the parametersMap.
	 */
	@Override
	void transformParams(HashMap<String, String> parametersMap)
	{
		String name	= this.getName();
		
		if (name != null && name.length() > 0)
		{
			parametersMap.remove(name);
		}
	}

}
