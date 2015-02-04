package ecologylab.bigsemantics.httpclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Test;

/**
 * 
 * @author quyin
 */
public class TestHttpResponseParser
{

  @Test
  public void testNormal() throws Exception
  {
    InputStream istream = getClass().getClassLoader().getResourceAsStream("testResponse-0.txt");
    SimplHttpResponse resp = SimplHttpResponse.parse("http://ecologylab.net", istream);
    assertEquals("http://ecologylab.net", resp.getUrl());
    assertNull(resp.getOtherUrls());
    assertEquals(200, resp.getCode());
    assertEquals("Wed, 04 Feb 2015 00:34:28 GMT", resp.getHeader("Date"));
    assertEquals("text/html", resp.getHeader("Content-Type"));
    String content = resp.getContent().trim();
    assertTrue(content.startsWith("<!DOCTYPE HTML"));
    assertTrue(content.endsWith("</html>"));
    assertTrue(content.contains("Using Metrics of Curation to Evaluate Information-based Ideation"));
  }

  @Test
  public void testNormal2() throws Exception
  {
    InputStream istream = getClass().getClassLoader().getResourceAsStream("testResponse-1.txt");
    SimplHttpResponse resp = SimplHttpResponse.parse("http://www.google.com", istream);
    assertEquals("http://www.google.com", resp.getUrl());
    assertNull(resp.getOtherUrls());
    assertEquals(200, resp.getCode());
    assertEquals("Wed, 04 Feb 2015 21:08:19 GMT", resp.getHeader("Date"));
    assertEquals("private, max-age=0", resp.getHeader("Cache-Control"));
    assertEquals("gws", resp.getHeader("Server"));
    assertEquals("chunked", resp.getHeader("Transfer-Encoding"));
    String content = resp.getContent().trim();
    assertTrue(content.startsWith("<!doctype html><html"));
    assertTrue(content.endsWith("</body></html>"));
    assertTrue(content.contains("Sign in"));
    assertTrue(content.contains("Language tools"));
    assertTrue(content.contains("I'm Feeling Lucky"));
  }

  @Test
  public void testRedirect() throws Exception
  {
    InputStream istream = getClass().getClassLoader().getResourceAsStream("testResponse-2.txt");
    SimplHttpResponse resp = SimplHttpResponse.parse("http://ecologylab.net/mice", istream);
    assertEquals("http://ecologylab.net/research/bigsemantics/MICE/", resp.getUrl());
    assertNotNull(resp.getOtherUrls());
    assertEquals(2, resp.getOtherUrls().size());
    assertTrue(resp.getOtherUrls().contains("http://ecologylab.net/mice"));
    assertTrue(resp.getOtherUrls().contains("http://ecologylab.net/research/bigsemantics/MICE"));
    assertEquals(200, resp.getCode());
    assertEquals("Wed, 04 Feb 2015 00:32:31 GMT", resp.getHeader("Date"));
    assertEquals("Apache", resp.getHeader("Server"));
    String content = resp.getContent().trim();
    assertTrue(content.startsWith("<!DOCTYPE html>"));
    assertTrue(content.endsWith("</html>"));
    assertTrue(content.contains("Metadata In-Context Expander"));
  }

  @Test
  public void testUnicode() throws Exception
  {
    InputStream istream = getClass().getClassLoader().getResourceAsStream("testResponse-3.txt");
    SimplHttpResponse resp = SimplHttpResponse.parse("http://en.wikipedia.org/wiki/China", istream);
    assertEquals("http://en.wikipedia.org/wiki/China", resp.getUrl());
    assertNull(resp.getOtherUrls());
    assertTrue(resp.getContent().contains("中国"));
  }

}
