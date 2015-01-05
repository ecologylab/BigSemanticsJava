package ecologylab.bigsemantics.example;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTypesScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * This is the most basic usage example of BigSemantics, that extracts a single metadata object from
 * a single URL. Newcomers to BigSemantics should start with this example.
 * 
 * @author quyin
 * 
 */
public class ExampleOne implements Continuation<DocumentClosure>
{

  /**
   * The SemanticsSessionScope encompasses all the information BigSemantics needs for accomplishing
   * tasks, such as the wrapper repository, document parsers, and download monitors.
   */
  SemanticsSessionScope sss;

  public ExampleOne()
  {
    /**
     * The metadataTypesScope contains descriptions of the generated metadata classes. These classes
     * are used to represent extracted metadata in program.
     */
    SimplTypesScope metadataTypesScope = RepositoryMetadataTypesScope.get();

    /**
     * DOM provider is used to convert a HTML stream into a DOM tree. BigSemantics by default uses
     * Cyberneko as its DOM provider, but you can use another one by writing a Wrapper (adapter)
     * class for it.
     */
    Class<CybernekoWrapper> domProviderClass = CybernekoWrapper.class;

    /**
     * Creates the SemanticsSessionScope. It will look up the wrapper repository in several places,
     * load all the wrapper definitions, and prepare all the objects that will be needed soon.
     */
    sss = new SemanticsSessionScope(metadataTypesScope,
                                    domProviderClass);
  }

  /**
   * Extracts the given URL. Note that because of the async nature of network operations, this
   * method does not directly return the extracted metadata. Instead, processing of the extracted
   * metadata needs to happen in the callback method.
   * 
   * @param url
   */
  public void extract(String url)
  {
    ParsedURL purl = ParsedURL.getAbsolute(url);
    Document doc = sss.getOrConstructDocument(purl);
    DocumentClosure closure = doc.getOrConstructClosure();
    closure.addContinuation(this);
    closure.queueDownload();
  }

  /**
   * The method that asynchronously processes extracted metadata objects.
   */
  @Override
  public void callback(DocumentClosure closure)
  {
    System.out.println("\n* * * * *\n");
    Document doc = closure.getDocument();
    System.out.println("The type of the extracted metadata: " + doc.getClass());
    SimplTypesScope.serializeOut(doc, "Extracted metadata", StringFormat.XML);
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    ExampleOne example = new ExampleOne();
    example.extract("http://dl.acm.org/citation.cfm?id=1871580");
  }

}
