package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Test;

public class TestRegexOp
{

  @Test
  public void testQuote()
  {
    RegexOp op = new RegexOp(Pattern.compile("\"([^\"]*)\""), 1);
    String result = op.operateOn("LCD Features:\n\" LED backlight\"");
    assertEquals("LED backlight", result);
  }

}
