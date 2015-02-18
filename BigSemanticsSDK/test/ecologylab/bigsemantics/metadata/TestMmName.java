package ecologylab.bigsemantics.metadata;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import ecologylab.bigsemantics.collecting.FakeSemanticsScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.downloadcontrollers.FakeDownloadControllerFactory;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTypesScope;
import ecologylab.bigsemantics.httpclient.SimplHttpResponse;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SimplTypesScope;

/**
 * Test if extracted metadata has the right mm_name associated with it, in different cases. The
 * correct mm_name should always be the name of the meta-metadata that is used for extracting that
 * metadata.
 * 
 * @author quyin
 */
public class TestMmName
{

  static SimplTypesScope               metadataScope;

  static FakeDownloadControllerFactory factory;

  static FakeSemanticsScope            semanticsScope;

  static
  {
    metadataScope = RepositoryMetadataTypesScope.get();
    factory = new FakeDownloadControllerFactory();
    semanticsScope = new FakeSemanticsScope(metadataScope, CybernekoWrapper.class);
    semanticsScope.setFakeDownloadControllerFactory(factory);
  }

  private SimplHttpResponse newDefaultResponse()
  {
    SimplHttpResponse response = new SimplHttpResponse();
    response.setCode(200);
    response.setMessage("OK");
    response.setContent("<html><head><title>Test Page</title></head><body></body></html>");
    return response;
  }

  private Document getDocument(String url) throws IOException
  {
    Document doc = semanticsScope.getOrConstructDocument(ParsedURL.getAbsolute(url));
    DocumentClosure closure = doc.getOrConstructClosure();
    closure.performDownloadSynchronously(false, false);
    doc = closure.getDocument();
    return doc;
  }

  /**
   * Test mm_name in regular cases.
   * 
   * @throws IOException
   */
  @Test
  public void testMmName() throws IOException
  {
    SimplHttpResponse response = newDefaultResponse();
    String url = "http://www.amazon.com/SomeProduct/dp/0000000000";
    factory.setResponse(url, response);

    Document doc = getDocument(url);
    assertEquals("amazon_product", doc.getMetaMetadataName());
  }

  /**
   * Test mm_name with non-type meta-metadata. Note that the URL used in this test case must be
   * associated with a non-type meta-metadata (a meta-metadata that does not define a new type).
   * 
   * @throws IOException
   */
  @Test
  public void testMmNameNonTypeMmd() throws IOException
  {
    SimplHttpResponse response = newDefaultResponse();
    String url = "http://www.nytimes.com/2014/05/15/news/some-random-news.html";
    factory.setResponse(url, response);

    Document doc = getDocument(url);
    assertEquals("nytimes", doc.getMetaMetadataName());
  }

  /**
   * Test mm_name when there is redirection.
   * 
   * @throws IOException
   */
  @Test
  public void testMmNameWithRedirection() throws IOException
  {
    SimplHttpResponse response = newDefaultResponse();
    String initialUrl = "http://redirect.com/";
    String realUrl = "http://dl.acm.org/citation.cfm?id=1234567";
    response.addOtherUrl(realUrl); // mimic redirection.
    factory.setResponse(initialUrl, response);

    Document doc = getDocument(initialUrl);
    assertEquals("acm_portal", doc.getMetaMetadataName());
  }

}
