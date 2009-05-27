/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.HashMap;

/**
 * @author amathur
 * 
 */
public class SemanticActionParameters {

	private HashMap<String,Object> standardObjectInstanceMap;
	
	public SemanticActionParameters(HashMap<String,Object> standardObjectInstanceMap)
	{
		this.standardObjectInstanceMap=standardObjectInstanceMap;
	}

	/**
	 * returns if the key is one of the standard object instances
	 * @param key
	 * @return
	 */
	public Object getObjectInstance(String key) {
		
		return standardObjectInstanceMap.get(key);
	}
	
			
}
