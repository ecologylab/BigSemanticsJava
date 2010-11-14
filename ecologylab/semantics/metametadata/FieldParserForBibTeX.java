package ecologylab.semantics.metametadata;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class FieldParserForBibTeX extends FieldParser
{

	public static final String	entryTypeTagName	= "@type";

	public static final String	entryIdTagName		= "@key";

	static Pattern							pEntry						= Pattern.compile(
																										"@(\\w+)\\s*\\{(.*)\\}");

	static Pattern							pString						= Pattern.compile(
																										"\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");

	static Map<String, String>	emptyMap					= new HashMap<String, String>();

	@Override
	public Map<String, String> getKeyValuePairResult(FieldParserElement parserElement, String input)
	{
		if (input == null)
			return emptyMap;
		return parseBibtexString(input.replaceAll("\\s+", " ").trim());
	}

	private Map<String, String> parseBibtexString(String input)
	{
		Map<String, String> rst = new HashMap<String, String>();

		Matcher matcher = pEntry.matcher(input);
		if (matcher.matches() && matcher.groupCount() == 2)
		{
			String entryType = matcher.group(1);
			rst.put(entryTypeTagName, entryType);

			String contents = matcher.group(2);
			String[] tags = contents.split(",");
			if (tags.length >= 1)
			{
				String entryId = tags[0].trim();
				rst.put(entryIdTagName, entryId);
				for (int i = 1; i < tags.length; ++i)
				{
					String tag = tags[i];
					String[] parts = tag.split("=");
					if (parts.length == 2)
					{
						String tagName = parts[0].trim();
						String tagValue0 = parts[1].trim();
						if (tagValue0.startsWith("\""))
						{
							// detect string concatenation
							StringBuilder sb = new StringBuilder();
							Matcher m = pString.matcher(tagValue0);
							while (m.find())
							{
								sb.append(m.group(1));
							}
							String tagValue = sb.toString();
							rst.put(tagName, tagValue);
						}
						else if (tagValue0.startsWith("{"))
						{
							// escape from curly braces
							int len = tagValue0.length();
							String tagValue = tagValue0.substring(1, len - 1);
							rst.put(tagName, tagValue);
						}
						else
						{
							// treat it as it is
							rst.put(tagName, tagValue0);
						}
					}
				}
			}
		}

		return rst;
	}

	@Test
	public void test()
	{
		String[] tests =
		{
				"",
				null,
				"@type  { \n    author:2000 , a1 = \"abc\\\"def\", \n a2= {stuff like this \n} , a3 =  \"part1\" # \" \\\"and\\\" part2.\" }  \t\n",
				"@ARTICLE{Gruber93atranslation,\n" +
						"author = {Thomas R. Gruber},\n" +
						"title = {A translation approach to portable ontology specifications},\n" +
						"journal = {KNOWLEDGE ACQUISITION},\n" +
						"year = 1993,\n" +
						"volume = {5},\n" +
						"pages = {199--220}\n" +
						"}\n",
		};

		for (String test : tests)
		{
			System.out.println();
			Map<String, String> bibtex = getKeyValuePairResult(null, test);
			for (String key : bibtex.keySet())
			{
				System.out.format("%s => %s\n", key, bibtex.get(key));
			}
		}
	}

}
