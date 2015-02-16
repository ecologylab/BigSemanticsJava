package ecologylab.bigsemantics;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestUtils
{

  public void genException()
  {
    throw new RuntimeException("This is a generated exception.");
  }

  @Test
  public void testStacktrace()
  {
    try
    {
      genException();
    }
    catch (Exception e)
    {
      String result = Utils.getStackTraceAsString(e);
      assertTrue(result.contains(TestUtils.class.getName()));
    }
  }

}
