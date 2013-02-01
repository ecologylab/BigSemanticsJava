/**
 * 
 */
package ecologylab.bigsemantics.dbinterface;

/**
 * Provides an instance of IDBDocumentProvider
 * and requires to be implemented with it
 * 
 * @author ajit
 *
 */

public interface IDocumentCacheFactory
{
	public IDocumentCache getDBDocumentProvider();
}
