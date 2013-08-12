package ecologylab.bigsemantics.documentcache;

/**
 * Interface to be implemented by caching mechanisms
 * 
 * @author colton
 */
public interface ISimplCache
{
	/**
	 * Tests if the specified key is present in the cache
	 * 
	 * @param key
	 *            the key to search by
	 * @return a boolean indicating the presence of the given key in the cache
	 */
	public boolean containsKey(String key);

	/**
	 * Gets the object mapped to the specified key. Returns <code>null</code> if
	 * the key is not mapped to an object
	 * 
	 * @param key
	 *            the key to search by
	 * @return the object mapped to the specified key, or <code>null</code> if
	 *         no mapping exists
	 */
	public Object get(String key);

	/**
	 * Gets the object mapped to the specified key. Returns <code>null</code> if
	 * the key is not mapped to an object
	 * 
	 * @param key
	 *            the key to search by
	 * @param revision
	 *            the revision number to retrieve
	 * @return the object mapped to the specified key, or <code>null</code> if
	 *         no mapping exists
	 */
	public Object get(String key, String revision);

	/**
	 * Maps the specified key to the object within the cache
	 * 
	 * @param key
	 *            the key to map the object to
	 * @param obj
	 *            the object to be added to the cache
	 */
	public void put(String key, Object obj);

	/**
	 * Checks first if the given key is already associated with an object. If no
	 * mapping exists, the object is added to the cache and a mapping is created
	 * 
	 * @param key
	 *            the key to search by
	 * @param obj
	 *            the Simpl-serializable object to potentially be added to the
	 *            cache
	 * @return the object mapped to the key
	 */
	public Object putIfAbsent(String key, Object obj);

	/**
	 * Replaces the object which a key is mapped to, only if the key is mapped
	 * to the object also indicated in the call
	 * 
	 * @param key
	 *            the key to search by
	 * @param oldObj
	 *            the Simpl-serializable object which is thought to be
	 *            associated with the key
	 * @param newObj
	 *            the Simpl-serializable object with which to replace the
	 *            original object with if conditions are satisfied
	 * @return the object mapped to the key, or <code>null</code> if no previous
	 *         mapping existed
	 */
	public boolean replace(String key, Object oldObj, Object newObj);

	/**
	 * Replaces the entry for a key if it is already mapped to some other entry
	 * 
	 * @param key
	 *            the key to search by
	 * @param newObj
	 *            the object to be associated with the key
	 * @return the previous object that was mapped to the key, or
	 *         <code>null</code> if no previous mapping existed
	 */
	public Object replace(String key, Object newObj);

	/**
	 * Removes the key and entry pair from the cache, if the key exists
	 * 
	 * @param key
	 *            the key to search by
	 */
	public void remove(String key);

	/**
	 * Removes the key and entry from the cache, only if the key maps to the
	 * object also given in the call
	 * 
	 * @param key
	 *            the key to search by
	 * @param oldObj
	 *            the object which is expected to be associated with the key
	 * @return a boolean indicating if the conditions were met, and a deletion
	 *         occurred
	 */
	public boolean remove(String key, Object oldObj);
}
