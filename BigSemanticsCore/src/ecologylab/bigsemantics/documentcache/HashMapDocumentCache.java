package ecologylab.bigsemantics.documentcache;

import java.util.concurrent.ConcurrentHashMap;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.net.ParsedURL;

/**
 * A Document cache using ConcurrentHashMap.
 * 
 * @author colton
 */
public class HashMapDocumentCache implements DocumentCache<ParsedURL, Document>
{

  private ConcurrentHashMap<ParsedURL, Document> map = new ConcurrentHashMap<ParsedURL, Document>();

  /**
   * Tests if the specified key is present in the cache
   * 
   * @param key
   *          the key to search by
   * @return a boolean indicating the presence of the given key in the cache
   */
  @Override
  public boolean containsKey(ParsedURL key)
  {
    return map.containsKey(key);
  }

  /**
   * Gets the object mapped to the specified key. Returns <code>null</code> if the key is not mapped
   * to an object
   * 
   * @param key
   *          the key to search by
   * @return the object mapped to the specified key, or <code>null</code> if no mapping exists
   */
  @Override
  public Document get(ParsedURL key)
  {
    return map.get(key);
  }

  /**
   * Throws an UnsupportedOperationException, as ConcurrentHashMap does not support the concept of
   * revisions
   * 
   * @param key
   *          the key to search by
   * @param revision
   *          the revision number to retrieve
   * @return the requested object
   * @throws UnsupportedOperationException
   *           always
   */
  @Override
  public Document get(ParsedURL key, String revision)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Maps the specified key to the object within the cache
   * 
   * @param key
   *          the key to map the object to
   * @param obj
   *          the object to be added to the cache
   */
  @Override
  public void put(ParsedURL key, Document obj)
  {
    map.put(key, obj);
  }

  /**
   * Checks first if the given key is already associated with an object. If no mapping exists, the
   * object is added to the cache and a mapping is created
   * 
   * @param key
   *          the key to search by
   * @param obj
   *          the object to potentially be added to the cache
   * @return the object mapped to the key
   */
  @Override
  public Document putIfAbsent(ParsedURL key, Document obj)
  {
    map.putIfAbsent(key, obj);

    return map.get(key);
  }

  /**
   * Replaces the object which a key is mapped to, only if the key is mapped to the object also
   * indicated in the call
   * 
   * @param key
   *          the key to search by
   * @param oldObj
   *          the object which is thought to be associated with the key
   * @param newObj
   *          the object with which to replace the original object with if conditions are satisfied
   * @return the object mapped to the key, or <code>null</code> if no previous mapping existed
   */
  @Override
  public boolean replace(ParsedURL key, Document oldObj, Document newObj)
  {
    return map.replace(key, oldObj, newObj);
  }

  /**
   * Replaces the entry for a key if it is already mapped to some other entry
   * 
   * @param key
   *          the key to search by
   * @param newObj
   *          the object to be associated with the key
   * @return the previous object that was mapped to the key, or <code>null</code> if no previous
   *         mapping existed
   */
  @Override
  public Document replace(ParsedURL key, Document newObj)
  {
    return map.replace(key, newObj);
  }

  /**
   * Removes the key and entry pair from the cache, if the key exists
   * 
   * @param key
   *          the key to search by
   */
  @Override
  public void remove(ParsedURL key)
  {
    map.remove(key);
  }

  /**
   * Removes the key and entry from the cache, only if the key maps to the object also given in the
   * call
   * 
   * @param key
   *          the key to search by
   * @param oldObj
   *          the object which is expected to be associated with the key
   * @return a boolean indicating if the conditions were met, and a deletion occurred
   */
  @Override
  public boolean remove(ParsedURL key, Document obj)
  {
    return map.remove(key, obj);
  }

}
