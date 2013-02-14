package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecologylab.generic.Debug;

public class FieldParserForRegexSplit extends FieldParser
{

	public static final String	DEFAULT_KEY	= "$0";

	@Override
	public List<Map<String, String>> getCollectionResult(FieldParserElement parserElement, String input)
	{
		List<Map<String, String>> rst = new ArrayList<Map<String, String>>();

		try
		{
			if (input != null)
			{
				String[] parts = parserElement.getRegex().split(input);
				int a = parserElement.getBeginIndex();
				if (a == FieldParserElement.BAD_VALUE)
					a = 0;
				int b = parserElement.getEndIndex();
				if (b == FieldParserElement.BAD_VALUE)
					b = parts.length;
				if (a < 0)
					a += parts.length;
				if (b < 0)
					b += parts.length;
				for (int i = 0; i < parts.length; ++i)
				{
					if (a <= i && i < b)
					{
						Map<String, String> item = new HashMap<String, String>();
						String value = parts[i];
						if (value != null && parserElement.isTrim())
							value = value.trim();
						item.put(DEFAULT_KEY, parts[i]);
						rst.add(item);
					}
					else
						rst.add(null);
				}
			}
		}
		catch (Throwable e)
		{
			Debug.error(this, "field parser (regex_find) failed: " + e.getMessage());
			e.printStackTrace();
		}

		return rst;
	}

}
