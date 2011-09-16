package ecologylab.semantics.metametadata;

import java.util.regex.Pattern;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_other_tags;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

@simpl_inherit
@simpl_tag("field_parser")
public class FieldParserElement extends ElementState
{

	static int			BAD_VALUE					= Integer.MIN_VALUE;

	@simpl_scalar
	private String	name;

	@simpl_scalar
	@simpl_other_tags("regex_split")
	private String	regex;

	@simpl_scalar
	private String	regexFind;

	@simpl_scalar
	private boolean	forEachElement		= false;

	@simpl_scalar
	private int			beginIndex				= BAD_VALUE;

	@simpl_scalar
	private int			endIndex					= BAD_VALUE;

	@simpl_scalar
	private boolean	trim							= true;

	private Pattern	compiledRegex			= null;

	private Pattern	compiledRegexFind	= null;

	public FieldParserElement()
	{

	}

	FieldParserElement(String name, String regex)
	{
		this.name = name;
		this.regex = regex;
	}

	public String getName()
	{
		return name;
	}

	public Pattern getRegex()
	{
		if (compiledRegex == null && regex != null)
			compiledRegex = Pattern.compile(regex);
		return compiledRegex;
	}

	public Pattern getRegexFind()
	{
		if (compiledRegexFind == null && regexFind != null)
			compiledRegexFind = Pattern.compile(regexFind);
		return compiledRegexFind;
	}

	public boolean isForEachElement()
	{
		return forEachElement;
	}

	public int getBeginIndex()
	{
		return beginIndex;
	}

	public int getEndIndex()
	{
		return endIndex;
	}

	public boolean isTrim()
	{
		return trim;
	}

}
