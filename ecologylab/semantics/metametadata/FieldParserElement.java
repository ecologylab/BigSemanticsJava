package ecologylab.semantics.metametadata;

import java.util.regex.Pattern;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
@xml_tag("field_parser")
public class FieldParserElement extends ElementState
{

	@simpl_scalar
	private String	name;

	@simpl_scalar
	private String	regex;

	private Pattern	compiledRegex	= null;

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
		{
			compiledRegex = Pattern.compile(regex);
		}
		return compiledRegex;
	}

}
