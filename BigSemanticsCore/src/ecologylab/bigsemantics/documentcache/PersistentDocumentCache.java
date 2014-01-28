/**
 * 
 */
package ecologylab.bigsemantics.documentcache;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.net.ParsedURL;

/**
 * A persistent cache that provides storage and retrieval for documents.
 * 
 * @author ajit
 */
public interface PersistentDocumentCache<D extends Document>
{

  /**
   * Get the persistence metadata for a given document.
   * 
   * @param docId
   * @return
   */
  PersistenceMetadata getMetadata(String docId);

  /**
   * Get the persistence metadata for a given document.
   * 
   * @param location
   * @return
   */
  PersistenceMetadata getMetadata(ParsedURL location);

  /**
   * Stores a document and raw page into the persistent cache.
   * 
   * @param document
   * @param rawDocument
   * @param metadata
   *          The method will use some metadata fields as input (e.g. MIME type), and fill in other
   *          fields as output.
   * @return True if successfully stored the document, otherwise false.
   */
  boolean store(D document, String rawDocument, PersistenceMetadata metadata);

  /**
   * Retrieves a document by document ID.
   * 
   * @param docId
   * @return
   */
  D retrieve(String docId);

  /**
   * Retrieve a document by location.
   * 
   * @param location
   * @return
   */
  D retrieve(ParsedURL location);

  /**
   * Retrieve the raw web document by document ID.
   * 
   * @param docId
   * @return
   */
  String retrieveRaw(String docId);

  /**
   * Retrieve the raw web document by document location.
   * 
   * @param location
   * @return
   */
  String retrieveRaw(ParsedURL location);

  /**
   * Remove a document with the given docId from persistence. This will remove metadata and raw
   * document too.
   * 
   * @param docId
   * @return true if the document existed and operation was successful; otherwise false.
   */
  boolean remove(String docId);

  /**
   * Removes a document with the given location from persistence. This will remove metadata and raw
   * document too.
   * 
   * @param location
   * @return true if the document existed and operation was successful; otherwise false.
   */
  boolean remove(ParsedURL location);

}
