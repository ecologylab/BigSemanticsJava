/**
 * 
 */
package ecologylab.semantics.seeding;

import ecologylab.generic.Continuation;
import ecologylab.io.Downloadable;
import ecologylab.net.ParsedURL;

/**
 * An object that can be queued, and have its dispatch target set, as well as downloaded.
 * 
 * @author andruid
 */
public interface QandDownloadable<D> extends Downloadable
{
	public boolean queueDownload();
	
	/**
	 * Get the ParsedURL that the Downloadable was initially created with.
	 * This could change due to server-side re-directs, or, as in ACM Portal, through semantic actions
	 * that substitute in the location slot the PDF's PURL for one that referred to a metadata page.
	 *  
	 * @return
	 */
	public ParsedURL getInitialPURL();
	
	/**
	 * Keeps state about the search process, if this Container is a search result;
	 */
	public SearchResult searchResult();
}
