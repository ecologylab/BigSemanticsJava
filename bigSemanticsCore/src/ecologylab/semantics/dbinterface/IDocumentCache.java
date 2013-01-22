/**
 * 
 */
package ecologylab.semantics.dbinterface;

import ecologylab.net.ParsedURL;
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
	
	/**
	 * Removes document corresponding to this url
	 *
	 * @param url
	 */
	public void removeDocument(ParsedURL url);
}
