package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ecologylab.bigsemantics.metadata.scalar.types.MetadataParsedURLScalarType;
import ecologylab.bigsemantics.metadata.scalar.types.MetadataStringScalarType;
import ecologylab.serialization.SIMPLTranslationException;

/**
 * Test common inheritance functionalities.
 * 
 * @author quyin
 */
public class TestInheritance extends BaseMmdTest
{

  @Test
  public void testInheritingWholeField() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritance1.xml");

    NewInheritanceHandler handler = new NewInheritanceHandler();
    assertTrue(handler.handleMmdRepository(repo));

    MetaMetadataScalarField scalar = null;

    scalar = (MetaMetadataScalarField) getNestedField(repo, "document", "meta_metadata_name");
    assertNotNull(scalar);
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());
  }

  @Test
  public void testMergingAttributes() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritance1.xml");

    NewInheritanceHandler handler = new NewInheritanceHandler();
    assertTrue(handler.handleMmdRepository(repo));

    MetaMetadataScalarField scalar = null;
    MetaMetadataCollectionField collection = null;

    // metadata:

    scalar = (MetaMetadataScalarField) getNestedField(repo, "metadata", "meta_metadata_name");
    assertFalse(scalar.isHide());

    scalar = (MetaMetadataScalarField) getNestedField(repo, "metadata", "location");
    assertNull(scalar);

    collection =
        (MetaMetadataCollectionField) getNestedField(repo, "metadata", "other_locations");
    assertNull(collection);

    scalar = (MetaMetadataScalarField) getNestedField(repo, "metadata", "title");
    assertNull(scalar);

    // document

    scalar = (MetaMetadataScalarField) getNestedField(repo, "document", "meta_metadata_name");
    assertFalse(scalar.isHide());
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());

    scalar = (MetaMetadataScalarField) getNestedField(repo, "document", "location");
    assertFalse(scalar.isHide());

    collection =
        (MetaMetadataCollectionField) getNestedField(repo, "document", "other_locations");
    assertFalse(collection.isHide());

    scalar = (MetaMetadataScalarField) getNestedField(repo, "document", "title");
    assertNull(scalar);

    // rich_document

    scalar = (MetaMetadataScalarField) getNestedField(repo, "rich_document", "meta_metadata_name");
    assertTrue(scalar.isHide());
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());

    scalar = (MetaMetadataScalarField) getNestedField(repo, "rich_document", "location");
    assertTrue(scalar.isHide());
    assertEquals(MetadataParsedURLScalarType.class, scalar.getScalarType().getClass());

    collection =
        (MetaMetadataCollectionField) getNestedField(repo, "rich_document", "other_locations");
    assertTrue(collection.isHide());
    assertEquals(MetadataParsedURLScalarType.class, collection.getChildScalarType().getClass());

    scalar = (MetaMetadataScalarField) getNestedField(repo, "rich_document", "title");
    assertNotNull(scalar);
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());
    assertEquals("h1", scalar.getStyleName());
    assertEquals("location", scalar.getNavigatesTo());
    assertNull(scalar.getUseValueAsLabel());

    // creative_work

    scalar = (MetaMetadataScalarField) getNestedField(repo, "creative_work", "meta_metadata_name");
    assertTrue(scalar.isHide());
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());

    scalar = (MetaMetadataScalarField) getNestedField(repo, "creative_work", "location");
    assertTrue(scalar.isHide());
    assertEquals(MetadataParsedURLScalarType.class, scalar.getScalarType().getClass());

    collection =
        (MetaMetadataCollectionField) getNestedField(repo, "creative_work", "other_locations");
    assertTrue(collection.isHide());
    assertEquals(MetadataParsedURLScalarType.class, collection.getChildScalarType().getClass());

    scalar = (MetaMetadataScalarField) getNestedField(repo, "creative_work", "title");
    assertNotNull(scalar);
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());
    assertEquals("caption", scalar.getStyleName());
    assertEquals("location", scalar.getNavigatesTo());
    assertEquals("test_value", scalar.getUseValueAsLabel());
    assertEquals(1, scalar.getXpathsSize());
    assertEquals("//meta[@name='title']/@content", scalar.getXpath(0));
  }

  @Test
  public void testMergingChildren() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritance1.xml");

    NewInheritanceHandler handler = new NewInheritanceHandler();
    assertTrue(handler.handleMmdRepository(repo));

    MetaMetadataScalarField scalar = null;
    MetaMetadataCompositeField composite = null;
    MetaMetadataCollectionField collection = null;

    // creative_work.source

    composite = (MetaMetadataCompositeField) getNestedField(repo, "creative_work", "source");

    scalar = (MetaMetadataScalarField) composite.lookupChild("meta_metadata_name");
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());

    scalar = (MetaMetadataScalarField) composite.lookupChild("title");
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());
    assertEquals("h1", scalar.getStyleName());
    assertEquals("location", scalar.getNavigatesTo());
    assertEquals(2, scalar.getXpathsSize());
    assertEquals(".", scalar.getXpath(0));
    assertEquals("//meta[@name='title']/@content", scalar.getXpath(1));

    scalar = (MetaMetadataScalarField) composite.lookupChild("location");
    assertEquals(MetadataParsedURLScalarType.class, scalar.getScalarType().getClass());
    assertTrue(scalar.isHide());
    assertEquals(1, scalar.getXpathsSize());
    assertEquals("./@href", scalar.getXpath(0));

    scalar = (MetaMetadataScalarField) composite.lookupChild("description");
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());
    assertEquals("description", scalar.getStyleName());
    assertEquals(0, scalar.getXpathsSize());

    // creative_work.references

    collection = (MetaMetadataCollectionField) getNestedField(repo, "creative_work", "references");

    scalar = (MetaMetadataScalarField) collection.lookupChild("meta_metadata_name");
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());

    scalar = (MetaMetadataScalarField) collection.lookupChild("title");
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());
    assertEquals("reference_caption", scalar.getStyleName());
    assertEquals("location", scalar.getNavigatesTo());
    assertEquals(2, scalar.getXpathsSize());
    assertEquals(".", scalar.getXpath(0));
    assertEquals("//meta[@name='title']/@content", scalar.getXpath(1));

    scalar = (MetaMetadataScalarField) collection.lookupChild("location");
    assertEquals(MetadataParsedURLScalarType.class, scalar.getScalarType().getClass());
    assertTrue(scalar.isHide());
    assertEquals(1, scalar.getXpathsSize());
    assertEquals("./@href", scalar.getXpath(0));

    scalar = (MetaMetadataScalarField) collection.lookupChild("description");
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());
    assertEquals("abstract", scalar.getStyleName());
    assertEquals(1, scalar.getXpathsSize());
    assertEquals("../p[@class='abstract']", scalar.getXpath(0));
  }

  @Test
  public void testRecursiveDependencies() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritance2.xml");

    NewInheritanceHandler handler = new NewInheritanceHandler();
    assertTrue(handler.handleMmdRepository(repo));

    MetaMetadataScalarField scalar = null;
    MetaMetadataCompositeField composite = null;
    MetaMetadataCollectionField collection = null;

    // creative_work.citations

    collection = (MetaMetadataCollectionField) getNestedField(repo, "creative_work", "citations");

    scalar = (MetaMetadataScalarField) collection.lookupChild("meta_metadata_name");
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());

    scalar = (MetaMetadataScalarField) collection.lookupChild("title");
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());
    assertEquals("citation_caption", scalar.getStyleName());
    assertEquals("location", scalar.getNavigatesTo());
    assertEquals(2, scalar.getXpathsSize());
    assertEquals(".", scalar.getXpath(0));
    assertEquals("//meta[@name='title']/@content", scalar.getXpath(1));

    scalar = (MetaMetadataScalarField) collection.lookupChild("location");
    assertEquals(MetadataParsedURLScalarType.class, scalar.getScalarType().getClass());
    assertTrue(scalar.isHide());
    assertEquals(1, scalar.getXpathsSize());
    assertEquals("./@href", scalar.getXpath(0));

    scalar = (MetaMetadataScalarField) collection.lookupChild("description");
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());
    assertEquals("abstract", scalar.getStyleName());
    assertEquals(1, scalar.getXpathsSize());
    assertEquals("../p[@class='abstract']", scalar.getXpath(0));
  }

  @Test
  public void testInterdependencies() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritance2.xml");

    NewInheritanceHandler handler = new NewInheritanceHandler();
    assertTrue(handler.handleMmdRepository(repo));

    MetaMetadataScalarField scalar = null;
    MetaMetadataCompositeField composite = null;
    MetaMetadataCollectionField collection = null;
    
    // rich_document.title
    scalar = (MetaMetadataScalarField) getNestedField(repo, "rich_document", "title");
    assertEquals("title", scalar.getLabel());
    
    // creative_work.authors.title
    scalar = (MetaMetadataScalarField) getNestedField(repo, "creative_work", "authors", "title");
    assertEquals("name", scalar.getLabel());
    assertEquals(2, scalar.getXpathsSize());
    assertEquals("./text()", scalar.getXpath(0));
    assertEquals("//meta[@name='title']/@content", scalar.getXpath(1));
    
    collection =
        (MetaMetadataCollectionField) getNestedField(repo, "creative_work", "authors", "works");
    assertNotNull(collection);
    assertNotNull(collection.lookupChild("title"));
    assertNotNull(collection.lookupChild("authors"));
    assertNotNull(collection.lookupChild("source"));
    assertNotNull(collection.lookupChild("references"));
    assertNotNull(collection.lookupChild("citations"));
    assertNotNull(collection.lookupChild("authors").lookupChild("title"));
    assertNotNull(collection.lookupChild("source").lookupChild("title"));
    assertNotNull(collection.lookupChild("references").lookupChild("title"));
    assertNotNull(collection.lookupChild("citations").lookupChild("title"));
  }

}