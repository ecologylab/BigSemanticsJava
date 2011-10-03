package ecologylab.semantics.metametadata;

import java.util.regex.Pattern;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class RegexFilter extends ElementState
{

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	private String	regex;

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	private String	replace;

	private String	javaRegex;

	private String	javaReplace;
	
	/**
	 * Lazily evaluated Pattern object compiled from regex.
	 */
	private Pattern regexPattern;

	public RegexFilter()
	{
		
	}
	
	public RegexFilter(String regex, String replace)
	{
		this.regex = regex;
		this.replace = replace;
	}

	public Pattern getRegex()
	{
		if (regexPattern == null)
			if (regex != null)
				regexPattern = Pattern.compile(regex);
		return regexPattern;
	}

	public String getJavaRegex()
	{
		if (javaRegex == null && regex != null)
			javaRegex = regex.replaceAll("\\\\", "\\\\\\\\");
		return javaRegex;
	}

	public String getReplace()
	{
		return replace;
	}

	public String getJavaReplace()
	{
		if (javaReplace == null && replace != null)
			javaReplace = replace.replaceAll("\\\\", "\\\\\\\\");
		return javaReplace;
	}
	
}
