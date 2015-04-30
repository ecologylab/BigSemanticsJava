package ecologylab.bigsemantics.metametadata.fieldops;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestSubstring
{

  @Test
  public void testBegin()
  {
    String s = "After a storm comes a calm.";
    Substring ss = new Substring();
    ss.setBegin(8);
    assertEquals("storm comes a calm.", ss.operateOn(s));
  }

  @Test
  public void testEnd()
  {
    String s = "After a storm comes a calm.";
    Substring ss = new Substring();
    ss.setEnd(13);
    assertEquals("After a storm", ss.operateOn(s));
  }

  @Test
  public void testBeginEnd()
  {
    String s = "After a storm comes a calm.";
    Substring ss = new Substring();
    ss.setBegin(8);
    ss.setEnd(13);
    assertEquals("storm", ss.operateOn(s));
  }

  @Test
  public void testAfter()
  {
    String s = "After a storm comes a calm.";
    Substring ss = new Substring();
    ss.setAfter("a ");
    assertEquals("storm comes a calm.", ss.operateOn(s));
  }

  @Test
  public void testBefore()
  {
    String s = "After a storm comes a calm.";
    Substring ss = new Substring();
    ss.setBefore(" comes");
    assertEquals("After a storm", ss.operateOn(s));
  }

  @Test
  public void testAfterBefore()
  {
    String s = "After a storm comes a calm.";
    Substring ss = new Substring();
    ss.setAfter("a ");
    ss.setBefore(" c");
    assertEquals("storm comes a", ss.operateOn(s));
  }

  @Test
  public void testIncAfter()
  {
    String s = "After a storm comes a calm.";
    Substring ss = new Substring();
    ss.setInclusiveAfter("a ");
    assertEquals("a storm comes a calm.", ss.operateOn(s));
  }

  @Test
  public void testIncBefore()
  {
    String s = "After a storm comes a calm.";
    Substring ss = new Substring();
    ss.setInclusiveBefore(" comes");
    assertEquals("After a storm comes", ss.operateOn(s));
  }

  @Test
  public void testIncAfterBefore()
  {
    String s = "After a storm comes a calm.";
    Substring ss = new Substring();
    ss.setInclusiveAfter("a ");
    ss.setInclusiveBefore(" c");
    assertEquals("a storm comes a c", ss.operateOn(s));
  }

}
