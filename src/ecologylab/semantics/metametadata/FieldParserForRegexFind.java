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
		
		if (input != null)
		{
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
	
	public void test(String regex, String input)
	{
		System.out.println();
		FieldParserElement pe = new FieldParserElement("regex_find", Pattern.compile(regex));
		Map<String, String> rst = getKeyValuePairResult(pe, input);
		for (String key : rst.keySet())
		{
			System.out.format("%s => %s\n", key, rst.get(key));
		}
	}

	@Test
	public void testFind()
	{
		test(
				"(\\S.+?)\\. ",
				"2007. Mixed media, sound, pneumatics, robotics, elector magnetic beaters, dentist chair, electric guitar, computer, various control systems, 9' 10\" x 13' 1\" x 8' 2\" (118 x 157 x 98 cm) 5 min. Gift of the Julia Stoschek Foundation, D�sseldorf, and the Dunn Bequest. � 2011 Janet Cardiff and George Bures Miller. Photo: Ugarte & Lorena Lopez. Courtesy of the artist, Luhring Augustine, New York and Galerie Barbara Weiss, Berlin. "
		);
		
		String p = "([A-Z][a-z]*)\\s+([A-Z]\\.?\\s+)?(([A-Z][a-z]*){1,2}(-([A-Z][a-z]*){1,2})?)\\s+([a-z0-9_.-]+@[a-z0-9_.-]+\\s+)?(\\([^)]+\\))";
		test(
				p,
				"Fred Martin fredm@cs.uml.edu (Principal Investigator)"
		);
		test(
				p,
				"Michelle Scribner-MacLean (Co-Principal Investigator)"
		);
	}
	
}
