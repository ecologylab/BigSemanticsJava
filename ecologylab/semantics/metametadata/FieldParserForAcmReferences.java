package ecologylab.semantics.metametadata;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldParserForAcmReferences extends FieldParser
{

	public static final String	OTHER				= "$other";

	public static final String	TITLE				= "$title";

	public static final String	AUTHOR_LIST	= "$author_list";

	@Override
	public Map<String, String> getKeyValuePairResult(FieldParserElement parserElement, String input)
	{
		Map<String, String> result = new HashMap<String, String>();
		if (input == null || input.length() == 0)
			return result;
		
		int flavor = 0;
		if (input.contains(" , ") || input.contains("doi>"))
			flavor = 1;
		else if (input.matches("^[A-Z][a-z]+, [A-Z]\\..*"))
			flavor = 2;
		
		switch (flavor)
		{
		case 1:
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
			break;
		case 2:
			StringBuilder sbAuthors = new StringBuilder();
			Pattern pAuthor = Pattern.compile("[A-Z][a-z]+, [A-Z]\\.");
			Matcher m = pAuthor.matcher(input);
			int lastAuthorPos = 0;
			while (m.find())
			{
				String authorName = m.group(0);
				if (lastAuthorPos != 0)
					sbAuthors.append(" , ");
				sbAuthors.append(authorName);
				lastAuthorPos = m.end();
			}
			result.put(AUTHOR_LIST, sbAuthors.toString());
			
			int nextDotPos = input.indexOf('.', lastAuthorPos);
			String title = input.substring(lastAuthorPos, nextDotPos); 
			result.put(TITLE, title);
			
			String other = input.substring(nextDotPos);
			result.put(OTHER, other);
			break;
		default:
			result.put(TITLE, input);
		}

		return result;
	}
	
	public static void main(String[] args)
	{
		String[] tests = {
				"Hamming, R. The Art of Doing Science and Engineering: Learning to Learn. CRC Press, 1997, 35. {The original maxim is, of course, \"The purpose of computing is insight, not numbers.\"} ",
				"Karlson, A., Piatko, C., and Gersh, J. Semantic navigation in complex graphs. Interactive poster and demonstration. Abstract published in IEEE Symposium on Information Visualization Poster Compendium (Seattle, WA), 2003, 84--85. ",
				"George W. Furnas , Samuel J. Rauch, Considerations for information environments and the NaviQue workspace, Proceedings of the third ACM conference on Digital libraries, p.79-88, June 23-26, 1998, Pittsburgh, Pennsylvania, United States  [doi>10.1145/276675.276684]",
		};
		FieldParserForAcmReferences f = new FieldParserForAcmReferences();
		
		for (String test : tests)
		{
			Map<String, String> kv = f.getKeyValuePairResult(null, test);
			System.out.println(kv);
		}
	}

}
