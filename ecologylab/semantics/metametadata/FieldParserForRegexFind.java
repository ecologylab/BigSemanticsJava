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
		int j=0;
		while (m.find())
		{
			for (int i=0; i <= m.groupCount(); i++)
			{
				String value = m.group(i);
				rst.put(groupPrefix + j++, value);
			}
		}

		return rst;
	}

	@Test
	public void testFind()
	{
		String test = "2007. Mixed media, sound, pneumatics, robotics, elector magnetic beaters, dentist chair, electric guitar, computer, various control systems, 9' 10\" x 13' 1\" x 8' 2\" (118 x 157 x 98 cm) 5 min. Gift of the Julia Stoschek Foundation, Düsseldorf, and the Dunn Bequest. © 2011 Janet Cardiff and George Bures Miller. Photo: Ugarte & Lorena Lopez. Courtesy of the artist, Luhring Augustine, New York and Galerie Barbara Weiss, Berlin. ";
		//String test = "(2007). Ink on paper, 13 7/8 x 9 7/8\" (35.2 x 25.1 cm). Purchased with funds provided by Jill and Peter Kraus. © 2011 Bernd Ribbeck";
		FieldParserElement pe = new FieldParserElement("regex_find", "(\\S.+?)\\. "
				/*"([^. ]\\S.+?)(?=[.])"*/);
		Map<String, String> rst = getKeyValuePairResult(pe, test);
		for (String key : rst.keySet())
		{
			System.out.format("%s => %s\n", key, rst.get(key));
		}
	}

}
