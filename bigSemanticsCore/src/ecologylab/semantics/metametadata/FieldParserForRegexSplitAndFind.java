package ecologylab.semantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldParserForRegexSplitAndFind extends FieldParserForRegexSplit
{
	
	FieldParserForRegexFind regexFind = new FieldParserForRegexFind();

	@Override
	public List<Map<String, String>> getCollectionResult(FieldParserElement parserElement,
			String input)
	{
		List<Map<String, String>> splitResults = null;
		if (parserElement.getRegex() != null)
		{
			splitResults = super.getCollectionResult(parserElement, input);
		}
		else
		{
			splitResults = new ArrayList<Map<String,String>>();
			Map<String, String> onlyResult = new HashMap<String, String>();
			onlyResult.put(FieldParserForRegexSplit.DEFAULT_KEY, input);
			splitResults.add(onlyResult);
		}
		
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
