/**
 * 
 */
package ecologylab.semantics.dbinterface;

/**
 * Provides an instance of IDBDocumentProvider
 * and requires to be implemented with it
 * 
 * @author ajit
 *
 */

public interface IDBDocumentProviderFactory
{
	public IDBDocumentProvider getDBDocumentProvider();
}
