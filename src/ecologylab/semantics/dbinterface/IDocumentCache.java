/**
 * 
 */
package ecologylab.semantics.dbinterface;

import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;

/**
 * interface for database retrieval and storage of documents
 * 
 * @author ajit
 * 
 */

public interface IDocumentCache
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
}
