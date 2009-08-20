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

	private Scope<Object> standardObjectInstanceMap;
	
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
	
	/**
	 * Adds a new parameter
	 * @param key
	 * @param value
	 */
	public void addParameter(String key,Object value)
	{
		standardObjectInstanceMap.put(key, value);
	}
	
	/**
	 * returns true if parameter has the given key.
	 * @param key
	 * @return
	 */
	public boolean contains(Object key)
	{
		return standardObjectInstanceMap.containsKey(key);
	}
}
