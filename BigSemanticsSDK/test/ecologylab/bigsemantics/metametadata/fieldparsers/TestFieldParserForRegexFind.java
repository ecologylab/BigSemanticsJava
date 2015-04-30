package ecologylab.bigsemantics.metametadata.fieldparsers;

import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

import ecologylab.bigsemantics.metametadata.fieldparsers.FieldParserElement;
import ecologylab.bigsemantics.metametadata.fieldparsers.FieldParserForRegexFind;

public class TestFieldParserForRegexFind
{

  private void test(String regex, String input)
  {
    FieldParserForRegexFind fp = new FieldParserForRegexFind();

    System.out.println();
    FieldParserElement pe = new FieldParserElement("regex_find", Pattern.compile(regex));
    Map<String, String> rst = fp.getKeyValuePairResult(pe, input);
    for (String key : rst.keySet())
    {
      System.out.format("%s => %s\n", key, rst.get(key));
    }
  }

  @Test
  public void testFind()
  {
    test("(\\S.+?)\\. ", "2007. Mixed media, sound, pneumatics, robotics, elector magnetic beaters, dentist chair, electric guitar, computer, various control systems, 9' 10\" x 13' 1\" x 8' 2\" (118 x 157 x 98 cm) 5 min. Gift of the Julia Stoschek Foundation, D�sseldorf, and the Dunn Bequest. � 2011 Janet Cardiff and George Bures Miller. Photo: Ugarte & Lorena Lopez. Courtesy of the artist, Luhring Augustine, New York and Galerie Barbara Weiss, Berlin. ");

    String p = "([A-Z][a-z]*)\\s+([A-Z]\\.?\\s+)?(([A-Z][a-z]*){1,2}(-([A-Z][a-z]*){1,2})?)\\s+([a-z0-9_.-]+@[a-z0-9_.-]+\\s+)?(\\([^)]+\\))";
    test(p, "Fred Martin fredm@cs.uml.edu (Principal Investigator)");
    test( p, "Michelle Scribner-MacLean (Co-Principal Investigator)");
  }

}
