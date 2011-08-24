/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.ElementState;

/**
 * @author andruid
 *
 */
public class Regex extends ElementState
{
	@simpl_scalar
	Pattern				match;
	
	@simpl_scalar
	String				replace;
	
	ParsedURL perform(ParsedURL input)
	{
		ParsedURL result	= null;
		if (input != null && match != null)
		{
			String 		string	= input.toString();
			Matcher 	matcher	= match.matcher(string);
			if (matcher.find())
			{
				if (replace == null)
					replace						= "";
				String resultString	= matcher.replaceAll(replace);
				result							= ParsedURL.getAbsolute(resultString);
			}
		}
		return result;
	}
}
