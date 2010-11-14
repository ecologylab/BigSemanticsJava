package ecologylab.semantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class FieldParserForRegexSplit extends FieldParser
{

	public static final String	DEFAULT_KEY	= "$0";

	@Override
	public List<Map<String, String>> getCollectionResult(FieldParserElement parserElement, String input)
	{
		List<Map<String, String>> rst = new ArrayList<Map<String, String>>();

		if (input != null)
		{
			String[] parts = parserElement.getRegex().split(input);
			for (int i = 0; i < parts.length; ++i)
			{
				Map<String, String> item = new HashMap<String, String>();
				item.put(DEFAULT_KEY, parts[i]);
				rst.add(item);
			}
		}

		return rst;
	}

	@Test
	public void testSplit()
	{
		String test = "Douglass R. Cutting ,  David R. Karger ,  Jan O. Pedersen ,  John W. Tukey";

		FieldParserElement pe = new FieldParserElement("regex_split", "\\s*,\\s+");
		List<Map<String, String>> rst = getCollectionResult(pe, test);
		for (Map<String, String> obj : rst)
		{
			System.out.println(obj.get(DEFAULT_KEY));
		}
	}

}
