package ecologylab.bigsemantics.metametadata.fieldparsers;

import java.util.Map;

import org.junit.Test;

import ecologylab.bigsemantics.metametadata.fieldparsers.FieldParserForBibTeX;

public class TestFieldParserForBibTeX
{

	@Test
	public void test()
	{
	  FieldParserForBibTeX fp = new FieldParserForBibTeX();
	  
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
			Map<String, String> bibtex = fp.getKeyValuePairResult(null, test);
			for (String key : bibtex.keySet())
			{
				System.out.format("%s => %s\n", key, bibtex.get(key));
			}
		}
	}

}
