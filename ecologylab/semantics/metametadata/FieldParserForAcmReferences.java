package ecologylab.semantics.metametadata;

import java.util.HashMap;
import java.util.Map;

public class FieldParserForAcmReferences extends FieldParser
{

	public static final String	OTHER				= "$other";

	public static final String	TITLE				= "$title";

	public static final String	AUTHOR_LIST	= "$author_list";

	@Override
	public Map<String, String> getKeyValuePairResult(FieldParserElement parserElement, String input)
	{
		Map<String, String> result = new HashMap<String, String>();

		if (input != null)
		{
			String[] authorListAndOther = input.split("(?<=\\S),\\s", 2);
			if (authorListAndOther.length == 2)
			{
				String authorList = authorListAndOther[0];
				if (authorList != null)
					result.put(AUTHOR_LIST, authorList.trim());
				String other = authorListAndOther[1];
				if (other != null)
				{
					String[] titleAndOther = other.split(",\\s(?=[A-Z])", 2);
					if (titleAndOther.length == 2)
					{
						String title = titleAndOther[0];
						String other0 = titleAndOther[1];
						if (title != null)
							result.put(TITLE, title.trim());
						if (other0 != null)
							result.put(OTHER, other0.trim());
					}
				}
			}
		}

		return result;
	}

}
