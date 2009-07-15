/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.HashMap;

import ecologylab.collections.Scope;

/**
 * Optional class that exposes the Java application instances to XML
 * @author amathur
 * 
 */
public class SemanticActionParameters {

	private Scope standardObjectInstanceMap;
	
	public SemanticActionParameters(Scope standardObjectInstanceMap)
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
