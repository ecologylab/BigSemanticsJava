package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.metadata.scalar.types.MetadataStringScalarType;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Unit test for MetaMetadataCollectionField.
 * 
 * @author quyin
 */
public class TestMetaMetadataCollectionField
{
  
  private SimplTypesScope mmdScope;
  
  @Before
  public void setUp()
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
    MetaMetadataRepository.initializeTypes();
    mmdScope = MetaMetadataTranslationScope.get();
  }

  @Test
  public void testDeserializingChildScalarType() throws SIMPLTranslationException
  {
    String xml = "<collection name=\"specifications\" child_scalar_type=\"String\"/>";
    MetaMetadataCollectionField collection =
        (MetaMetadataCollectionField) mmdScope.deserialize(xml, StringFormat.XML);
    assertNotNull(collection);
    assertNotNull(collection.childScalarType);
    assertTrue(collection.childScalarType instanceof MetadataStringScalarType);
  }

}
