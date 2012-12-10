package ecologylab.semantics.metametadata;

import java.util.HashMap;
import java.util.Map;

/**
 * utility to manage field parsers.
 * 
 * @author quyin
 * 
 */
public class FieldParserFactory
{

	private Map<String, FieldParser>	registeredFieldParsers	= new HashMap<String, FieldParser>();

	public FieldParserFactory()
	{
		// built-in field parsers:
		registerFieldParser("bibtex", new FieldParserForBibTeX());
		registerFieldParser("regex_find", new FieldParserForRegexFind());
		registerFieldParser("regex_split", new FieldParserForRegexSplit());
		registerFieldParser("regex_split_and_find", new FieldParserForRegexSplitAndFind());
		registerFieldParser("acm_reference", new FieldParserForAcmReferences());
		registerFieldParser("nsf_pub_list", new FieldParserForNsfPubList());
	}

	public void registerFieldParser(String name, FieldParser fieldParser)
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
