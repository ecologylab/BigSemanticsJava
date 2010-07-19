package ecologylab.semantics.metametadata;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.Hint;

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

	public RegexFilter()
	{
		
	}
	
	public RegexFilter(String regex, String replace)
	{
		this.regex = regex;
		this.replace = replace;
	}

	public String getRegex()
	{
		return regex;
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
