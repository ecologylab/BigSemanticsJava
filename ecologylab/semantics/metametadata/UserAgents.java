/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.UserAgent;
import ecologylab.xml.ElementState;
import ecologylab.xml.ElementState.xml_map;

/**
 * @author amathur
 *
 */
public class UserAgents extends ElementState
{
	@xml_map("user_agent") private HashMapArrayList<String, UserAgent> userAgent; 
	
}
