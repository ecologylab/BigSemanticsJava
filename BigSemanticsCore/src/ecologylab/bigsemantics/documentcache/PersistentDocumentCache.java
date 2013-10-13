/**
 * 
 */
package ecologylab.bigsemantics.documentcache;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.net.ParsedURL;

/**
 * interface for database retrieval and storage of documents
 * 
 * @author ajit
 * 
 */

public interface PersistentDocumentCache
{
	/**
	 * Returns a document or null corresponding to location in DocumentClosure
	 * Currently, only location information is used for lookup.
	 * 
	 * @param closure
	 * @return Document
	 */
	public Document retrieveDocument(DocumentClosure closure);

	/**
	 * Stores the document in database
	 * 
	 * @param document
	 */
	public void storeDocument(Document document);
	
	/**
	 * Removes document corresponding to this url
	 *
	 * @param url
	 */
	public void removeDocument(ParsedURL url);
}
