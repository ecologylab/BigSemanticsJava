package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ecologylab.generic.StringTools;
import ecologylab.serialization.XMLTools;

public class FieldParserForNsfPubList extends FieldParserForAcmReferences
{
	
	@Override
	public List<Map<String, String>> getCollectionResult(FieldParserElement parserElement,
			String input)
	{
		input = input.replaceAll("\\s+", " ");
		input = StringTools.trimUntil(input, "</table>", true);
		input = StringTools.trimAfter(input, "</p>", true);
		
		input = XMLTools.removeHtmlTag(input, "script", false);
		input = XMLTools.removeHtmlTag(input, "noscript", false);
		input = XMLTools.removeHtmlTag(input, "i", true);
		
		String[] splits = input.split("(\\s*<br\\s*/?>\\s*)+");
		List<Map<String, String>> results = new ArrayList<Map<String,String>>();
		for (String split : splits)
		{
			if (split == null)
				continue;
			split = split.trim();
			if (split.length() == 0)
				continue;
			if (split.startsWith("<")) // must be some tag
				continue;
			
			split = XMLTools.unescapeXML(split);
			split = split.replaceFirst("^\\d+\\.\\s*", "");
			Map<String, String> parsedRef = super.getKeyValuePairResult(null, split);
			if (parsedRef != null && parsedRef.size() > 0)
			{
				System.out.println(parsedRef);
				results.add(parsedRef);
			}
		}
		
		return results;
	}
	
}
