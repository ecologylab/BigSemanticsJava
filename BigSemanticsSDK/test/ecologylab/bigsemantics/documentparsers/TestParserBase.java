package ecologylab.bigsemantics.documentparsers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

import ecologylab.bigsemantics.BaseMmdTest;
import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.downloadcontrollers.DefaultDownloadController;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTypesScope;
import ecologylab.bigsemantics.html.dom.IDOMProvider;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.collections.Scope;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Unit test for extracting scalar collections.
 * 
 * @author quyin
 */
public class TestParserBase
{

  static class FakeParserBase extends ParserBase<Document>
  {

    @Override
    public Document populateMetadata(Document document,
                                     MetaMetadataCompositeField metaMetadata,
                                     org.w3c.dom.Document dom,
                                     SemanticActionHandler handler) throws IOException
    {
      return null;
    }

  }

  static class TestProduct extends Document
  {

    @simpl_collection("spec")
    List<String> specs;

  }

  private SimplTypesScope       mmdScope;

  private SemanticsSessionScope semanticsSessionScope;

  private FakeParserBase        parser;

  @Before
  public void setUp()
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
    MetaMetadataRepository.initializeTypes();
    mmdScope = MetaMetadataTranslationScope.get();
    semanticsSessionScope =
        new SemanticsSessionScope(RepositoryMetadataTypesScope.get(), CybernekoWrapper.class);
    parser = new FakeParserBase();
    parser.setSemanticsScope(semanticsSessionScope);
  }

  @Test
  public void testExtractScalarCollection() throws SIMPLTranslationException, IOException
  {
    String mmdXml =
        "<meta_metadata name=\"test_product\">" +
        "  <collection name=\"specs\" child_scalar_type=\"String\"" +
        "   xpath=\"//ul[@id='specs-list']/li\"/>" +
        "</meta_metadata>";
    MetaMetadata mmd = (MetaMetadata) mmdScope.deserialize(mmdXml, StringFormat.XML);
    assertNotNull(mmd);
    MetaMetadataField field = mmd.getChildrenMap().get("specs");
    assertNotNull(field);

    ClassDescriptor classDescriptor = ClassDescriptor.getClassDescriptor(TestProduct.class);
    FieldDescriptor fieldDescriptor = classDescriptor.getFieldDescriptorByFieldName("specs");
    assertTrue(fieldDescriptor instanceof MetadataFieldDescriptor);
    BaseMmdTest.setMetadataFieldDescriptor(field, (MetadataFieldDescriptor) fieldDescriptor);

    TestProduct testProduct = new TestProduct();

    IDOMProvider domProvider = semanticsSessionScope.constructDOMProvider();
    InputStream testDocStream =
        getClass().getResourceAsStream("/ecologylab/bigsemantics/documentparsers/testProduct.html");
    assertNotNull(testDocStream);
    Node docNode = domProvider.parseDOM(testDocStream, null);
    Map<String, String> fieldParserContext = new HashMap<String, String>();
    Scope<Object> params = new Scope<Object>();

    parser.setDownloadController(new DefaultDownloadController());
    parser.handler = new SemanticActionHandler(semanticsSessionScope, parser);
    boolean success =
        parser.recursiveExtraction(mmd, testProduct, docNode, fieldParserContext, params);

    assertTrue(success);
    assertNotNull(testProduct.specs);
    System.out.println(testProduct.specs);
  }

}
