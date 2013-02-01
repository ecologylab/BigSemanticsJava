/**
 * 
 */
package ecologylab.bigsemantics.metametadata;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.UserAgent;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_map;

/**
 * @author amathur
 *
 */
public class UserAgents extends ElementState
{
	@simpl_map("user_agent") private HashMapArrayList<String, UserAgent> userAgent; 
	
}
