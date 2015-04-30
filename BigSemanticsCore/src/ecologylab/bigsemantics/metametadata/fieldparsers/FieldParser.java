package ecologylab.bigsemantics.metametadata.fieldparsers;

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

	public Map<String, String> getKeyValuePairResult(FieldParserElement parserElement, String input)
	{
		return null;
	}

	public List<Map<String, String>> getCollectionResult(FieldParserElement parserElement, String input)
	{
		return null;
	}

}
