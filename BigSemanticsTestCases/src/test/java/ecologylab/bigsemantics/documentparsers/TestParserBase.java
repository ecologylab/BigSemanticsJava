package ecologylab.bigsemantics.documentparsers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.documentparsers.ParserBase;
import ecologylab.bigsemantics.downloadcontrollers.DefaultDownloadController;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.html.dom.IDOMProvider;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.FieldUtils;
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
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_scalar;
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

  public static class TestProduct extends Document
  {
    
    @simpl_composite
    TestProduct  org;

    @simpl_collection("spec")
    List<String> specs;
    
    public TestProduct()
    {
      this(null);
    }
    
    public TestProduct(MetaMetadataCompositeField mmd)
    {
      super();
    }

  }

  private static SimplTypesScope       mmdScope;

  private static SemanticsSessionScope semanticsSessionScope;

  private FakeParserBase               parser;

  @BeforeClass
  public static void init()
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
    MetaMetadataRepository.initializeTypes();
    mmdScope = MetaMetadataTranslationScope.get();
    semanticsSessionScope =
        new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(), CybernekoWrapper.class);
  }
  
  @Before
  public void setup()
  {
    parser = new FakeParserBase();
    parser.setSemanticsScope(semanticsSessionScope);
  }
  
  private void bindMetadataFieldDescriptor(MetaMetadata mmd,
                                           Class<? extends Metadata> metadataClass,
                                           String fieldName)
  {
    assertNotNull(mmd);
    MetaMetadataField field = mmd.getChildMetaMetadata().get(fieldName);
    assertNotNull(field);
    ClassDescriptor classDescriptor = ClassDescriptor.getClassDescriptor(metadataClass);
    FieldDescriptor fieldDescriptor =
        classDescriptor.getFieldDescriptorByFieldName(field.getName());
    assertTrue(fieldDescriptor instanceof MetadataFieldDescriptor);
    FieldUtils.setMetadataFieldDescriptor(field, (MetadataFieldDescriptor) fieldDescriptor);
    
    if (field instanceof MetaMetadataCompositeField)
    {
      ClassDescriptor fieldClassDescriptor = fieldDescriptor.getElementClassDescriptor();
      FieldUtils.setMetadataClassDescriptor(field, (MetadataClassDescriptor) fieldClassDescriptor);
    }
  }
  
  @Test
  public void testExtractScalarCollection() throws SIMPLTranslationException, IOException
  {
    String mmdXml =
        "<meta_metadata name='test_product'>" +
        "  <collection name='specs' child_scalar_type='String'>" +
        "    <xpath>//ul[@id='specs-list']/li</xpath>" +
        "  </collection>" +
        "</meta_metadata>";
    MetaMetadata mmd = (MetaMetadata) mmdScope.deserialize(mmdXml, StringFormat.XML);
    bindMetadataFieldDescriptor(mmd, TestProduct.class, "specs");

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
  }

  @Test
  public void testSecondaryXpath() throws SIMPLTranslationException, IOException
  {
    String mmdXml =
        "<meta_metadata name='test_product'>" +
        "  <scalar name='description'>" +
        "    <xpath>//div[@class='description']</xpath>" +
        "    <xpath>//meta[@name='description']/@content</xpath>" +
        "  </scalar>" +
        "  <composite name='org' type='test_product'>" +
        "    <xpath>//div[@itemtype='blabla']</xpath>" +
        "    <xpath>//div[@itemtype='http://schema.org/Organization']</xpath>" +
        "    <scalar name='description'>" +
        "      <xpath>.//meta/@content</xpath>" +
        "    </scalar>" +
        "  </composite>" +
        "  <collection name='specs' child_scalar_type='String'>" +
        "    <xpath>//ul[@id='specs']/li</xpath>" +
        "    <xpath>//ul[@id='specs-list']/li</xpath>" +
        "  </collection>" +
        "</meta_metadata>";
    MetaMetadata mmd = (MetaMetadata) mmdScope.deserialize(mmdXml, StringFormat.XML);
    bindMetadataFieldDescriptor(mmd, TestProduct.class, "description");
    bindMetadataFieldDescriptor(mmd, TestProduct.class, "org");
    bindMetadataFieldDescriptor(mmd, TestProduct.class, "specs");
    
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
    assertNotNull(testProduct.getDescription());
    assertNotNull(testProduct.org);
    assertNotNull(testProduct.specs);
  }

}
