package ecologylab.semantics.metametadata;

import java.util.regex.Pattern;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_other_tags;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * The element allowing extracting information from a flat string.
 * 
 * This is just the element used for holding information required by the extraction process.
 * 
 * For the actual extraction functionality, see {@link FieldParser} and derivative classes.
 * 
 * @author quyin
 *
 */
@simpl_inherit
@simpl_tag("field_parser")
public class FieldParserElement extends ElementState
{

	static int			BAD_VALUE					= Integer.MIN_VALUE;

	/**
	 * The name of the parser, e.g. regex_find
	 */
	@simpl_scalar
	private String	name;

	/**
	 * The regex used by the parser, if needed.
	 * 
	 * For regex_find, this is the regex that will be matched (using find()). For regex_split and
	 * regex_split_and_find, this is the regex that delimits each element in the list.
	 */
	@simpl_scalar
	@simpl_other_tags("regex_split")
	private Pattern	regex;

	/**
	 * This field is only used with regex_split_and_find, in which case regex_split will be the
	 * delimiter and this will be the matching one.
	 */
	@simpl_scalar
	private Pattern	regexFind;

	/**
	 * This should be set to true, if the field_parser is going to be applied to a collection of
	 * flat strings.
	 * 
	 * When it is true, the xpath / tag name will be used to generate a set of input strings, which
	 * will then be used for each element in the collection respectively.
	 * 
	 * When it is false, the xpath / tag name will be used to generate a single input string, which
	 * will then be used to create a set of values for each element in the collection.
	 */
	@simpl_scalar
	private boolean	forEachElement		= false;

	@simpl_scalar
	private int			beginIndex				= BAD_VALUE;

	@simpl_scalar
	private int			endIndex					= BAD_VALUE;

	@simpl_scalar
	private boolean	trim							= true;

	public FieldParserElement()
	{

	}

	FieldParserElement(String name, Pattern regex)
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
		return regex;
	}

	public Pattern getRegexFind()
	{
		return regexFind;
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
