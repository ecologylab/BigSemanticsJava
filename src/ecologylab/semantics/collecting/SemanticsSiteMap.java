/**
 * 
 */
package ecologylab.semantics.collecting;

import java.util.concurrent.ConcurrentHashMap;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;

/**
 * @author andrew
 *
 */
public class SemanticsSiteMap extends ConcurrentHashMap<String, SemanticsSite>
{
	
	public SemanticsSite getOrConstruct(Document document, SemanticsGlobalScope infoCollector) 
	{
		ParsedURL parsedURL	= document.getLocation();
		String domain				= parsedURL == null ? null : parsedURL.domain();
		SemanticsSite result	= null;
		if (domain != null)
		{
			result							= this.get(domain);
			if (result == null) 
			{
				// record does not yet exist
				SemanticsSite newRec	= new SemanticsSite(domain, infoCollector);
				result = this.putIfAbsent(domain, newRec);
				if (result == null)
				{
					// put succeeded, use new value
					result = newRec;
				}
			}
		}
		return result;
	}
	
}
