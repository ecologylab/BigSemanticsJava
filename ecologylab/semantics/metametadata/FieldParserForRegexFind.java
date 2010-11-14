package ecologylab.semantics.metametadata;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class FieldParserForRegexFind extends FieldParser
{
	static final String	groupPrefix	= "$";

	@Override
	public Map<String, String> getKeyValuePairResult(FieldParserElement parserElement, String input)
	{
		Map<String, String> rst = new HashMap<String, String>();

		Pattern p = parserElement.getRegex();
		Matcher m = p.matcher(input);
		while (m.find())
		{
			for (int i = 0; i <= m.groupCount(); ++i)
			{
				String value = m.group(i);
				rst.put(groupPrefix + i, value);
			}
		}

		return rst;
	}

	@Test
	public void testFind()
	{
		String test = "Scatter/Gather: A Cluster-based Approach to Browsing Large Document Collections (1992) [465 citations — 12 self]";
		FieldParserElement pe = new FieldParserElement("regex_find",
				"\\[(\\d+) citations — (\\d+) self\\]");
		Map<String, String> rst = getKeyValuePairResult(pe, test);
		for (String key : rst.keySet())
		{
			System.out.format("%s => %s\n", key, rst.get(key));
		}
	}

}
