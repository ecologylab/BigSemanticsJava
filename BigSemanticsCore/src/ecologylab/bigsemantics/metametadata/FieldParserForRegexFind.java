package ecologylab.bigsemantics.metametadata;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldParserForRegexFind extends FieldParser
{
	static final String	groupPrefix	= "$";

	@Override
	public Map<String, String> getKeyValuePairResult(FieldParserElement parserElement, String input)
	{
		Map<String, String> rst = new HashMap<String, String>();
		
		if (input != null)
		{
			if (parserElement.isNormalizeText())
			{
				input = input.replaceAll("\\s+", " ").trim();
			}
			
			Pattern p = parserElement.getRegexFind();
			if (p == null || p.pattern() == null || p.pattern().length() == 0)
				p = parserElement.getRegex();
			Matcher m = p.matcher(input);
			int j=0;
			while (m.find())
			{
				for (int i=0; i <= m.groupCount(); i++)
				{
					String value = m.group(i);
					rst.put(groupPrefix + j++, value);
				}
			}
		}

		return rst;
	}
	
}
