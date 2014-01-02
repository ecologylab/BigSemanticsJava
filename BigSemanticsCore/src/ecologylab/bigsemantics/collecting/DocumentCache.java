package ecologylab.bigsemantics.collecting;

import ecologylab.bigsemantics.metadata.builtins.Document;

/**
 * A cache for instances of Document or its subclasses.
 * 
 * The key is a generic type because in different use cases different kinds of keys can be used: for
 * the local document cache the location (ParsedURL) is used, but for database persistence a hash
 * string is used.
 * 
 * @author colton
 */
public interface DocumentCache<K, D extends Document>
{

  /**
   * Tests if the specified key is present in the cache.
   * 
   * @param key
   *          the key to search by.
   * @return a boolean indicating the presence of the given key in the cache.
   */
  public boolean containsKey(K key);

  /**
   * Gets the document mapped to the specified key. Returns <code>null</code> if the key is not
   * mapped to an document.
   * 
   * @param key
   *          the key to search by.
   * @return the document mapped to the specified key, or <code>null</code> if no mapping exists.
   */
  public D get(K key);

  /**
   * Gets the document mapped to the specified key. Returns <code>null</code> if the key is not
   * mapped to an document.
   * 
   * @param key
   *          the key to search by.
   * @param revision
   *          the revision number to retrieve.
   * @return the document mapped to the specified key, or <code>null</code> if no mapping exists.
   */
  public D get(K key, String revision);

  /**
   * Maps the specified key to the document within the cache.
   * 
   * @param key
   *          the key to map the document to.
   * @param document
   *          the document to be added to the cache.
   */
  public void put(K key, D document);

  /**
   * Checks first if the given key is already associated with an document. If no mapping exists, the
   * document is added to the cache and a mapping is created.
   * 
   * @param key
   *          the key to search by.
   * @param document
   *          the document to potentially be added to the cache.
   * @return the previous document mapped to this key, or <code>null</code> if there was not such a
   *         mapping.
   */
  public D putIfAbsent(K key, D document);

  /**
   * Replaces the document which a key is mapped to, only if the key is mapped to the document also
   * indicated in the call.
   * 
   * @param key
   *          the key to search by.
   * @param oldDocument
   *          the document which is supposed to be associated with the key.
   * @param newDocument
   *          the document with which to replace the original document with if conditions are
   *          satisfied.
   * @return the document previously mapped to the key, or <code>null</code> if no previous mapping
   *         existed.
   */
  public boolean replace(K key, D oldDocument, D newDocument);

  /**
   * Replaces the entry for a key if it is already mapped to some other entry.
   * 
   * @param key
   *          the key to search by.
   * @param newDocument
   *          the document to be associated with the key.
   * @return the previous document that was mapped to the key, or <code>null</code> if no previous
   *         mapping existed.
   */
  public D replace(K key, D newDocument);

  /**
   * Removes the key and entry pair from the cache, if the key exists.
   * 
   * @param key
   *          the key to search by.
   */
  public void remove(K key);

  /**
   * Removes the key and entry from the cache, only if the key maps to the document also given in
   * the call.
   * 
   * @param key
   *          the key to search by.
   * @param oldDocument
   *          the document which is expected to be associated with the key.
   * @return a boolean indicating if the conditions were met, and a deletion occurred.
   */
  public boolean remove(K key, D oldDocument);

}
