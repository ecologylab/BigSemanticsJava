/**
 * 
 */
package ecologylab.semantics.collecting;

import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.ElementState;

/**
 * @author andruid
 *
 */
public class CookieProcessing extends ElementState
{
	@simpl_scalar
	String		domain;
	
	@simpl_scalar
	boolean		ignoreAllCookies;
	
	static 
	HashMap<String, Boolean>		globalCookieAcceptance	= new HashMap<String, Boolean>();
	
	/**
	 * 
	 */
	public CookieProcessing()
	{
	}

	
	@Override
	public void deserializationPostHook()
	{
		System.out.println("Setting cookie policy for domain : " + domain + " [IgnoreAllCookies: " + ignoreAllCookies + "]");
		if (domain != null)
			globalCookieAcceptance.put(domain, ignoreAllCookies);
	}
	
	public static CookiePolicy semanticsCookiePolicy	= new CookiePolicy()
	{

		@Override
		//TODO -- find a magic way to make this more efficient or send java se programmers to a dungeon
		public boolean shouldAccept(URI uri, HttpCookie cookie)
		{
			ParsedURL purl	= ParsedURL.get(uri);
			String domain		= purl.domain();
			Boolean result	= globalCookieAcceptance.get(domain);
			purl.recycle();
			
			//result = (result == null) || result;
			//System.out.println("MMD has Cookie rejected from URI: [" + result + "] :" + uri );
			
			return (result == null) || result;
		}
		
	};
}
