package ecologylab.bigsemantics.metametadata.fieldparsers;

import java.util.HashMap;
import java.util.Map;

/**
 * utility to manage field parsers.
 * 
 * @author quyin
 * 
 */
public class FieldParserRegistry
{

	private Map<String, FieldParser>	registeredFieldParsers	= new HashMap<String, FieldParser>();

	public FieldParserRegistry()
	{
		// built-in field parsers:
		register("bibtex", new FieldParserForBibTeX());
		register("regex_find", new FieldParserForRegexFind());
		register("regex_split", new FieldParserForRegexSplit());
		register("regex_split_and_find", new FieldParserForRegexSplitAndFind());
		register("acm_reference", new FieldParserForAcmReferences());
		register("nsf_pub_list", new FieldParserForNsfPubList());
	}

	public void register(String name, FieldParser fieldParser)
	{
		registeredFieldParsers.put(name, fieldParser);
	}

	public FieldParser get(String name)
	{
		if (registeredFieldParsers.containsKey(name))
			return registeredFieldParsers.get(name);
		else
			return null;
	}

}
