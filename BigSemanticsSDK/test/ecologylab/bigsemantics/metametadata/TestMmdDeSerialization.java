package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.actions.ForEachSemanticAction;
import ecologylab.bigsemantics.actions.IfSemanticAction;
import ecologylab.bigsemantics.actions.ParseDocumentSemanticAction;
import ecologylab.bigsemantics.metadata.scalar.types.SemanticsTypes;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.StringFormat;

public class TestMmdDeSerialization
{
  
  @Before
  public void init()
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
    new SemanticsTypes();
  }
  
  @Test
  public void testDeserializeSemanticActions() throws SIMPLTranslationException
  {
    String mmdStr =
        "<meta_metadata>" +
        "  <semantic_actions>" +
        "    <if></if>" +
        "    <for_each></for_each>" +
        "    <parse_document></parse_document>" +
        "    <reselect_meta_metadata_and_extract></reselect_meta_metadata_and_extract>" +
        "  </semantic_actions>" +
        "</meta_metadata>";
    
    SimplTypesScope mmdTScope = MetaMetadataTranslationScope.get();
    MetaMetadata mmd = (MetaMetadata) mmdTScope.deserialize(mmdStr, StringFormat.XML);
    SimplTypesScope.serializeOut(mmd, "mmd: ", StringFormat.XML);
    assertNotNull(mmd.getSemanticActions());
    assertEquals(3, mmd.getSemanticActions().size());
    assertTrue(mmd.getSemanticActions().get(0) instanceof IfSemanticAction);
    assertTrue(mmd.getSemanticActions().get(1) instanceof ForEachSemanticAction);
    assertTrue(mmd.getSemanticActions().get(2) instanceof ParseDocumentSemanticAction);
  }
  
//  public static void main(String[] args) throws SIMPLTranslationException, FileNotFoundException
//  {
//    MetaMetadataRepositoryLoader loader = new MetaMetadataRepositoryLoader();
//    
//    File repositoryDir = MetaMetadataRepositoryLocator.locateRepositoryByDefaultLocations();
//    
//    MetaMetadataRepository repository = loader.loadFromDir(repositoryDir, Format.XML);
//    repository.traverseAndInheritMetaMetadata();
//    
//    MetaMetadata acmMmd = repository.getMMByName("acm_portal");
//    
//    SimplTypesScope.serialize(acmMmd, new File("c:/tmp/test_mmd.xml"), Format.XML);
//    
//    MetaMetadataField authorsField = acmMmd.getChildMetaMetadata().get("authors");
//    
//    System.out.println(authorsField);
//  }
  
  @Test
  public void testSerializeSuperFieldInJson() throws SIMPLTranslationException
  {
    SimplTypesScope mmdTScope = MetaMetadataTranslationScope.get();

    MetaMetadata mmd = new MetaMetadata();
    mmd.setName("test_mmd");

    MetaMetadataScalarField baseField = new MetaMetadataScalarField();
    baseField.setName("base_field");

    MetaMetadataScalarField field = new MetaMetadataScalarField();
    field.setName("field");

    field.setSuperField(baseField);
    field.setDeclaringMmd(mmd);
    
    String xml = SimplTypesScope.serialize(field, StringFormat.XML).toString();
    MetaMetadataScalarField f =
        (MetaMetadataScalarField) mmdTScope.deserialize(xml, StringFormat.XML);
    assertNotNull(f.getSuperField());
    assertEquals("base_field", f.getSuperField().getName());
    assertNotNull(f.getDeclaringMmd());
    assertEquals("test_mmd", f.getDeclaringMmd().getName());

    String json = SimplTypesScope.serialize(field, StringFormat.JSON).toString();
    System.out.println(json);
    assertTrue(json.contains("inherited_field"));
    assertTrue(json.contains("declaring_mmd"));
    
    MetaMetadataScalarField newField =
        (MetaMetadataScalarField) mmdTScope.deserialize(json, StringFormat.JSON);
    assertNotNull(newField.getSuperField());
    assertEquals("base_field", newField.getSuperField().getName());
    assertNotNull(newField.getDeclaringMmd());
    assertEquals("test_mmd", newField.getDeclaringMmd().getName());
  }

}
