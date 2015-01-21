/**
 * 
 */
package ecologylab.bigsemantics.documentcache;

import org.apache.commons.configuration.Configuration;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.PersistenceMetaInfo;
import ecologylab.net.ParsedURL;

/**
 * A persistent cache that provides storage and retrieval for documents.
 * 
 * @author ajit
 * @author quyin
 */
public interface PersistentDocumentCache<D extends Document>
{
  
  void configure(Configuration configs) throws Exception;

  /**
   * Get the persistence metadata for a given document.
   * 
   * @param location
   * @return The meta info for the given location, or null if not found in the cache.
   */
  PersistenceMetaInfo getMetaInfo(ParsedURL location) throws Exception;
  
  /**
   * Stores a document and raw page content into the persistent cache.
   * 
   * @param document
   * @param rawContent
   * @param charset
   * @param mimeType
   * @param mmdVersion
   * @param mmdHash
   * @return The corresponding PersistenceMetaInfo object.
   */
  PersistenceMetaInfo store(D document, //metadata
                            String rawContent, //html
                            String charset,
                            String mimeType,
                            String mmdHash) throws Exception;
  
  /**
   * Update the cached document. Keeps the cache raw page content unchanged. 
   * 
   * @param metaInfo
   * @param newDoc
   * @return true if the operation was successful; otherwise false.
   */
  boolean updateDoc(PersistenceMetaInfo metaInfo, D newDoc) throws Exception;//metadata

  /**
   * Retrieve a document.
   * 
   * @param metaInfo
   * @return The doc, or null if not found.
   */
  D retrieveDoc(PersistenceMetaInfo metaInfo) throws Exception;

  /**
   * Retrieve the raw page content.
   * 
   * @param metaInfo
   * @return
   */
  String retrieveRawContent(PersistenceMetaInfo metaInfo) throws Exception;

  /**
   * Removes a document and corresponding raw page content from the cacle.
   * 
   * @param metaInfo
   * @return true if the operation was successful; otherwise false.
   */
  boolean remove(PersistenceMetaInfo metaInfo) throws Exception;
  
  //boolean updateRawDoc maybe...

}
