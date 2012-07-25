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

public interface IDBDocumentProvider
{
	public Document retrieveDocument(DocumentClosure closure);
	public void storeDocument(Document document);
}
