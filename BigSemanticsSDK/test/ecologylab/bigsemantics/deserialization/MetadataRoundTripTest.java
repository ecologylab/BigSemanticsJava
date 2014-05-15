package ecologylab.bigsemantics.deserialization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.MetadataDeserializationHookStrategy;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Testing serializing and deserializing metadata.
 * 
 * @author
 * @author quyin
 */
public class MetadataRoundTripTest
{

  static SimplTypesScope       metadataScope;

  static SemanticsSessionScope semanticsScope;

  static
  {
    metadataScope = RepositoryMetadataTranslationScope.get();
    semanticsScope = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(),
                                               CybernekoWrapper.class);
  }

  public String serializationHelper(Object obj) throws SIMPLTranslationException
  {
    String xml = SimplTypesScope.serialize(obj, StringFormat.XML).toString();

    File tempDir = PropertiesAndDirectories.tempDir();
    File tempFile = new File(tempDir, "metadata-roundtrip.xml");
    SimplTypesScope.serialize(obj, tempFile, Format.XML);
    System.out.println("metadata serialized to " + tempFile);

    return xml;
  }

  public Document deserializationHelper(String xml) throws SIMPLTranslationException
  {
    DeserializationHookStrategy deserializationHookStrategy =
        new MetadataDeserializationHookStrategy(semanticsScope);
    Document doc = (Document) metadataScope.deserialize(xml,
                                                        deserializationHookStrategy,
                                                        StringFormat.XML);
    assertNotNull(doc);
    assertNotNull("deserialized metadata doesn't have a meta-metadata associated!",
                  doc.getMetaMetadata());
    return doc;
  }

  @Test
  public void testYelpBusiness() throws SIMPLTranslationException, IOException
  {
    ParsedURL purl = ParsedURL.getAbsolute("http://dl.acm.org/citation.cfm?id=1979124");
    Document doc = semanticsScope.getOrConstructDocument(purl);
    DocumentClosure closure = doc.getOrConstructClosure();
    closure.performDownloadSynchronously();
    doc = closure.getDocument();
    assertNotNull(doc);

    String xml = serializationHelper(doc);
    Document doc2 = (Document) deserializationHelper(xml);
    assertNotNull(doc2);
    
    assertSame(doc.getClass(), doc2.getClass());
    // TODO compare the two metadata objects field wise.
  }

}
