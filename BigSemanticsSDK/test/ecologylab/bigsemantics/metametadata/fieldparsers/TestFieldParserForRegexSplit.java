package ecologylab.bigsemantics.metametadata.fieldparsers;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import ecologylab.bigsemantics.metametadata.fieldparsers.FieldParserElement;
import ecologylab.bigsemantics.metametadata.fieldparsers.FieldParserForRegexSplit;

public class TestFieldParserForRegexSplit
{

	@Test
	public void testSplit()
	{
	  FieldParserForRegexSplit fp = new FieldParserForRegexSplit();
	  
		String test = "2007. Mixed media, sound, pneumatics, robotics, elector magnetic beaters, dentist chair, electric guitar, computer, various control systems, 9 10 x 13 1 x 8 2 (118 x 157 x 98 cm) 5 min. Gift of the Julia Stoschek Foundation, D�sseldorf, and the Dunn Bequest. � 2011 Janet Cardiff and George Bures Miller. Photo: Ugarte & Lorena Lopez. Courtesy of the artist, Luhring Augustine, New York and Galerie Barbara Weiss, Berlin. ";

		FieldParserElement pe = new FieldParserElement("regex_split", Pattern.compile("\\s*\\.\\s+"));
		List<Map<String, String>> rst = fp.getCollectionResult(pe, test);
		for (Map<String, String> obj : rst)
		{
			System.out.println(obj.get(FieldParserForRegexSplit.DEFAULT_KEY));
		}
	}

}
