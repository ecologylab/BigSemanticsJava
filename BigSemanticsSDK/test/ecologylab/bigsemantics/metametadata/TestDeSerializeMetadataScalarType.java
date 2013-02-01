package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.metadata.scalar.types.MetadataParsedURLScalarType;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.StringFormat;

public class TestDeSerializeMetadataScalarType
{
  
  @Before
  public void init()
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
    MetaMetadataRepository.initializeTypes();
  }
  
  @Test
  public void testDeSerializeMetadataScalarType() throws SIMPLTranslationException
  {
    MetaMetadataCollectionField collection = new MetaMetadataCollectionField();
    collection.childScalarType = new MetadataParsedURLScalarType();
    String xml = SimplTypesScope.serialize(collection, StringFormat.XML).toString();
    assertNotNull(xml);
    System.out.println("========>\t" + xml);
    
    SimplTypesScope mmdTScope = MetaMetadataTranslationScope.get();
    collection = (MetaMetadataCollectionField) mmdTScope.deserialize(xml, StringFormat.XML);
    assertNotNull(collection.childScalarType);
    assertTrue(collection.childScalarType instanceof MetadataParsedURLScalarType);
  }

}
