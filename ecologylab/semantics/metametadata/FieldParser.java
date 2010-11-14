package ecologylab.semantics.metametadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * abstraction for field parsers. a field parser parses the input string either to a Map or to a
 * List.
 * <p />
 * note that the implemented parsers have to be stateless and thread-safe, because each of them will
 * be used in multiple threads with different countexts.
 * 
 * @author quyin
 * 
 */
public abstract class FieldParser
{

	static Map<String, FieldParser>	preDefinedFieldParsers	= new HashMap<String, FieldParser>();

	static
	{
		preDefinedFieldParsers.put("bibtex", new FieldParserForBibTeX());
		preDefinedFieldParsers.put("regex_find", new FieldParserForRegexFind());
		preDefinedFieldParsers.put("regex_split", new FieldParserForRegexSplit());
	}
	
	public static FieldParser get(String name)
	{
		if (preDefinedFieldParsers.containsKey(name))
			return preDefinedFieldParsers.get(name);
		else
			return null;
	}

	public Map<String, String> getKeyValuePairResult(FieldParserElement parserElement, String input)
	{
		return null;
	}

	public List<Map<String, String>> getCollectionResult(FieldParserElement parserElement, String input)
	{
		return null;
	}

}
