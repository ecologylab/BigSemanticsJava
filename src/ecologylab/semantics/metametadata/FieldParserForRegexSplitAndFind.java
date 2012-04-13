package ecologylab.semantics.metametadata;

import java.util.List;
import java.util.Map;

public class FieldParserForRegexSplitAndFind extends FieldParserForRegexSplit
{
	
	FieldParserForRegexFind regexFind = new FieldParserForRegexFind();

	@Override
	public List<Map<String, String>> getCollectionResult(FieldParserElement parserElement,
			String input)
	{
		List<Map<String, String>> splitResults = super.getCollectionResult(parserElement, input);
		if (splitResults != null)
		{
			for (Map<String, String> splitResult : splitResults)
			{
				if (splitResult.size() > 0)
				{
					String split = splitResult.values().iterator().next();
					splitResult.clear();
					splitResult.putAll(regexFind.getKeyValuePairResult(parserElement, split));
				}
			}
		}
		
		return splitResults;
	}

}
