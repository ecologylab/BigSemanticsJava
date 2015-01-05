package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import ecologylab.bigsemantics.BaseMmdTest;

/**
 * 
 * @author quyin
 */
public class TestInheritanceWithRealRepository extends BaseMmdTest
{

  @Test
  public void testLowesProduct()
  {
    MetaMetadataRepository realRepo = loadRealRepository();

    MetaMetadataCollectionField collection = null;

    collection =
        (MetaMetadataCollectionField) getNestedField(realRepo, "lowes_product", "product_details");
    assertNotNull(collection.getXpaths());
    assertEquals(1, collection.getXpathsSize());
  }

}
