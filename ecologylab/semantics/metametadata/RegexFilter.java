package ecologylab.semantics.metametadata;

import java.util.regex.Pattern;

import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.StringFormat;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class RegexFilter extends ElementState
{

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	private Pattern	regex;

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	private String	replace;

	private String	javaRegex;

	private String	javaReplace;
	
	public RegexFilter()
	{
		
	}
	
	public RegexFilter(Pattern regex, String replace)
	{
		this.regex = regex;
		this.replace = replace;
	}

	public Pattern getRegex()
	{
		return regex;
	}

	public String getJavaRegex()
	{
		if (javaRegex == null && regex != null)
			javaRegex = regex.pattern().replaceAll("\\\\", "\\\\\\\\");
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
	
	public static void main(String[] args)
	{
		String[] testPatterns = {
			"\\s+",
			"\\\\\\\\ 4 back slashes",
		};
		String testReplace = "";
		for (String p : testPatterns)
		{
			RegexFilter rf = new RegexFilter(Pattern.compile(p), testReplace);
			System.out.println();
			ClassDescriptor.serializeOut(rf, "some message", StringFormat.XML);
			System.out.println();
			System.out.println("In java annotation: " + rf.getJavaRegex());
			System.out.println();
		}
	}
	
}
