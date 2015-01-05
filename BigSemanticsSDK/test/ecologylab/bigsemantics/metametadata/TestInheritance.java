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
public class TestInheritance extends TestInheritanceIssues
{

  @Test
  public void testInheritingWholeField() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritance1.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadataScalarField scalar = null;

    scalar = (MetaMetadataScalarField) getNestedField(repo, "document", "meta_metadata_name");
    assertNotNull(scalar);
    assertEquals(MetadataStringScalarType.class, scalar.getScalarType().getClass());
  }

  @Test
  public void testMergingAttributes() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritance1.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

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

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

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

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadataScalarField scalar = null;
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

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadataScalarField scalar = null;
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

  @Test
  public void testChangingTypeOnSubFields() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritance2.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadataScalarField scalar = null;
    MetaMetadataCollectionField collection = null;
    MetaMetadata mmd = null;

    // rich_document.description
    scalar = (MetaMetadataScalarField) getNestedField(repo, "rich_document", "description");
    assertEquals("description", scalar.getLabel());

    // scholarly_article
    mmd = (MetaMetadata) getNestedField(repo, "scholarly_article");
    assertNotNull(mmd.lookupChild("title"));
    assertNotNull(mmd.lookupChild("location"));
    assertNotNull(mmd.lookupChild("description"));
    assertNotNull(mmd.lookupChild("authors"));
    assertNotNull(mmd.lookupChild("references"));
    assertNotNull(mmd.lookupChild("citations"));
    assertNotNull(mmd.lookupChild("doi"));
    assertNotNull(mmd.lookupChild("journal"));

    // scholarly_article.description
    scalar = (MetaMetadataScalarField) mmd.lookupChild("description");
    assertEquals("abstract", scalar.getLabel());

    // scholarly_article.authors
    collection = (MetaMetadataCollectionField) mmd.lookupChild("authors");
    assertNotNull(collection.lookupChild("title"));
    assertNotNull(collection.lookupChild("location"));
    assertNotNull(collection.lookupChild("works"));
    assertNotNull(collection.lookupChild("affiliation"));

    // scholar
    mmd = (MetaMetadata) getNestedField(repo, "scholar");
    assertNotNull(mmd.lookupChild("title"));
    assertNotNull(mmd.lookupChild("location"));
    assertNotNull(mmd.lookupChild("works"));
    assertNotNull(mmd.lookupChild("affiliation"));

    // scholar.works
    collection = (MetaMetadataCollectionField) getNestedField(repo, "scholar", "works");
    assertNotNull(collection.lookupChild("title"));
    assertNotNull(collection.lookupChild("location"));
    assertNotNull(collection.lookupChild("description"));
    assertNotNull(collection.lookupChild("authors"));
    assertNotNull(collection.lookupChild("references"));
    assertNotNull(collection.lookupChild("citations"));
    assertNotNull(collection.lookupChild("doi"));
    assertNotNull(collection.lookupChild("journal"));
  }

  @Test
  public void testInlineMmd() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritance3.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadata mmd = null;
    MetaMetadata inlineMmd = null;
    MmdScope scope = null;
    MetaMetadataCollectionField collection = null;
    MetaMetadataCompositeField composite = null;

    // creative_work.scope
    mmd = (MetaMetadata) getNestedField(repo, "creative_work");
    scope = mmd.getScope();
    assertNotNull(scope);
    inlineMmd = (MetaMetadata) scope.get("author");
    assertNotNull(inlineMmd);
    assertNotNull(inlineMmd.lookupChild("works"));

    // creative_work.authors.works
    collection =
        (MetaMetadataCollectionField) getNestedField(repo, "creative_work", "authors", "works");
    assertNotNull(collection.lookupChild("title"));
    assertNotNull(collection.lookupChild("location"));
    assertNotNull(collection.lookupChild("description"));
    assertNotNull(collection.lookupChild("authors"));
    assertNotNull(collection.lookupChild("source"));
    assertNotNull(collection.lookupChild("references"));
    assertNotNull(collection.lookupChild("citations"));

    // scholarly_article.scope
    mmd = (MetaMetadata) getNestedField(repo, "scholarly_article");
    scope = mmd.getScope();
    assertNotNull(scope);
    inlineMmd = (MetaMetadata) scope.get("scholar");
    assertNotNull(inlineMmd);
    assertNotNull(inlineMmd.lookupChild("works"));
    inlineMmd = (MetaMetadata) scope.get("journal");
    assertNotNull(inlineMmd);
    assertNotNull(inlineMmd.lookupChild("publisher"));

    // scholarly_article.authors
    collection = (MetaMetadataCollectionField) getNestedField(repo, "scholarly_article", "authors");
    assertNotNull(collection.lookupChild("title"));
    assertNotNull(collection.lookupChild("location"));
    assertNotNull(collection.lookupChild("description"));
    assertNotNull(collection.lookupChild("works"));
    assertNotNull(collection.lookupChild("affiliation"));

    // scholarly_article.journal
    composite = (MetaMetadataCompositeField) getNestedField(repo, "scholarly_article", "journal");
    assertNotNull(composite.lookupChild("title"));
    assertNotNull(composite.lookupChild("location"));
    assertNotNull(composite.lookupChild("description"));
    assertNotNull(composite.lookupChild("authors"));
    assertNotNull(composite.lookupChild("source"));
    assertNotNull(composite.lookupChild("references"));
    assertNotNull(composite.lookupChild("citations"));
  }

  @Test
  public void testGenerics1() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testGenerics1.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadataCollectionField collection = null;

    // search.search_results
    collection = (MetaMetadataCollectionField) getNestedField(repo, "search", "search_results");
    assertNotNull(collection.lookupChild("meta_metadata_name"));
    assertNotNull(collection.lookupChild("location"));
    assertNotNull(collection.lookupChild("other_locations"));
    assertEquals("document", collection.getTypeMmd().getName());

    // google_search.search_results
    collection =
        (MetaMetadataCollectionField) getNestedField(repo, "google_search", "search_results");
    assertNotNull(collection.lookupChild("meta_metadata_name"));
    assertNotNull(collection.lookupChild("location"));
    assertNotNull(collection.lookupChild("other_locations"));
    assertEquals("document", collection.getTypeMmd().getName());

    // image_search.search_results
    collection =
        (MetaMetadataCollectionField) getNestedField(repo, "image_search", "search_results");
    assertNotNull(collection.lookupChild("meta_metadata_name"));
    assertNotNull(collection.lookupChild("location"));
    assertNotNull(collection.lookupChild("other_locations"));
    assertNotNull(collection.lookupChild("width"));
    assertNotNull(collection.lookupChild("height"));
    assertEquals("image", collection.getTypeMmd().getName());

    // google_image_search.search_results
    collection =
        (MetaMetadataCollectionField) getNestedField(repo, "google_image_search", "search_results");
    assertNotNull(collection.lookupChild("meta_metadata_name"));
    assertNotNull(collection.lookupChild("location"));
    assertNotNull(collection.lookupChild("other_locations"));
    assertNotNull(collection.lookupChild("width"));
    assertNotNull(collection.lookupChild("height"));
    assertEquals("google_image", collection.getTypeMmd().getName());

    // google_image_search.search_results.source_page
    MetaMetadataField composite = collection.lookupChild("source_page");
    assertNotNull(composite);
    assertNotNull(composite.lookupChild("meta_metadata_name"));
    assertNotNull(composite.lookupChild("location"));
    assertNotNull(composite.lookupChild("other_locations"));
    assertEquals("document", composite.getTypeMmd().getName());
  }

  @Test
  public void testGenerics2() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testGenerics2.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadataCompositeField composite = null;
    MetaMetadataCollectionField collection = null;
    MetaMetadataCompositeField comp = null;
    MetaMetadataCollectionField coll = null;

    // foo.foo_1
    composite = (MetaMetadataCompositeField) getNestedField(repo, "foo", "foo_1");
    assertNotNull(composite.lookupChild("meta_metadata_name"));
    assertEquals("metadata", composite.getTypeMmd().getName());

    // foo.foo_2
    collection = (MetaMetadataCollectionField) getNestedField(repo, "foo", "foo_2");
    assertNotNull(collection.lookupChild("location"));
    assertEquals("document", collection.getTypeMmd().getName());

    // bar.foo_1
    composite = (MetaMetadataCompositeField) getNestedField(repo, "bar", "foo_1");
    assertNotNull(composite.lookupChild("meta_metadata_name"));
    assertEquals("metadata", composite.getTypeMmd().getName());

    // bar.foo_2
    collection = (MetaMetadataCollectionField) getNestedField(repo, "bar", "foo_2");
    assertNotNull(collection.lookupChild("location"));
    assertEquals("document", collection.getTypeMmd().getName());

    // base.f1
    composite = (MetaMetadataCompositeField) getNestedField(repo, "base", "f1");
    assertNotNull(composite.lookupChild("meta_metadata_name"));
    assertEquals("metadata", composite.getTypeMmd().getName());

    // base.f2
    composite = (MetaMetadataCompositeField) getNestedField(repo, "base", "f2");
    assertNotNull(composite.lookupChild("title"));
    assertEquals("rich_doc", composite.getTypeMmd().getName());

    // base.f3
    composite = (MetaMetadataCompositeField) getNestedField(repo, "base", "f3");
    assertNotNull(composite.lookupChild("foo_1"));
    assertNotNull(composite.lookupChild("foo_2"));
    assertEquals("foo", composite.getTypeMmd().getName());

    // base.f3.foo_1 (extends metadata)
    comp = (MetaMetadataCompositeField) composite.lookupChild("foo_1");
    assertNotNull(comp);
    assertNotNull(comp.lookupChild("meta_metadata_name"));
    assertEquals("metadata", comp.getTypeMmd().getName());

    // base.f3.foo_2 (extends rich_doc)
    coll = (MetaMetadataCollectionField) composite.lookupChild("foo_2");
    assertNotNull(coll);
    assertNotNull(coll.lookupChild("title"));
    assertEquals("rich_doc", coll.getTypeMmd().getName());

    // base.f4
    collection = (MetaMetadataCollectionField) getNestedField(repo, "base", "f4");
    assertNotNull(collection.lookupChild("meta_metadata_name"));
    assertEquals("metadata", collection.getTypeMmd().getName());

    // base.f5
    collection = (MetaMetadataCollectionField) getNestedField(repo, "base", "f5");
    assertNotNull(collection.lookupChild("title"));
    assertEquals("rich_doc", collection.getTypeMmd().getName());

    // sub.f1
    composite = (MetaMetadataCompositeField) getNestedField(repo, "sub", "f1");
    assertNotNull(composite.lookupChild("meta_metadata_name"));
    assertEquals("metadata", composite.getTypeMmd().getName());

    // sub.f2
    composite = (MetaMetadataCompositeField) getNestedField(repo, "sub", "f2");
    assertNotNull(composite.lookupChild("images"));
    assertEquals("richer_doc", composite.getTypeMmd().getName());

    // sub.f3
    composite = (MetaMetadataCompositeField) getNestedField(repo, "sub", "f3");
    assertNotNull(composite.lookupChild("foobar"));
    assertEquals("bar", composite.getTypeMmd().getName());

    // sub.f3.foo_1 (bound to foo<document, document>)
    comp = (MetaMetadataCompositeField) composite.lookupChild("foo_1");
    assertNotNull(comp);
    assertNotNull(comp.lookupChild("foo_1"));
    assertNotNull(comp.lookupChild("foo_2"));
    assertEquals("foo", comp.getTypeMmd().getName());

    // sub.f3.foo_1.foo_1
    assertNotNull(comp.lookupChild("foo_1").lookupChild("location"));
    assertEquals("document", comp.lookupChild("foo_1").getTypeMmd().getName());

    // sub.f3.foo_2 (extends richer_doc)
    coll = (MetaMetadataCollectionField) composite.lookupChild("foo_2");
    assertNotNull(coll);
    assertNotNull(coll.lookupChild("images"));
    assertEquals("richer_doc", coll.getTypeMmd().getName());

    // sub.f4
    collection = (MetaMetadataCollectionField) getNestedField(repo, "sub", "f4");
    assertNotNull(collection.lookupChild("location"));
    assertEquals("document", collection.getTypeMmd().getName());

    // sub.f5
    collection = (MetaMetadataCollectionField) getNestedField(repo, "sub", "f5");
    assertNotNull(collection.lookupChild("images"));
    assertEquals("richer_doc", collection.getTypeMmd().getName());

    // sub.f6
    collection = (MetaMetadataCollectionField) getNestedField(repo, "sub", "f6");
    assertNotNull(collection.lookupChild("meta_metadata_name"));
    assertEquals("metadata", collection.getTypeMmd().getName());
  }

  @Test
  public void testGenerics3() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testGenerics3.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadataCompositeField composite = null;
    MetaMetadataCollectionField collection = null;

    // foo.cats
    collection = (MetaMetadataCollectionField) getNestedField(repo, "foo", "cats");
    assertNotNull(collection.lookupChild("meta_metadata_name"));
    assertEquals("metadata", collection.getTypeMmd().getName());

    // foo.dogs
    collection = (MetaMetadataCollectionField) getNestedField(repo, "foo", "dogs");
    assertNotNull(collection.lookupChild("location"));
    assertEquals("document", collection.getTypeMmd().getName());

    // bar.baz
    composite = (MetaMetadataCompositeField) getNestedField(repo, "bar", "baz");
    assertNotNull(composite.lookupChild("cats"));
    assertNotNull(composite.lookupChild("dogs"));
    assertEquals("foo", composite.getTypeMmd().getName());

    // bar.baz.cats
    collection = (MetaMetadataCollectionField) composite.lookupChild("cats");
    assertNotNull(collection.lookupChild("location"));
    assertEquals("document", collection.getTypeMmd().getName());

    // bar.baz.dogs
    collection = (MetaMetadataCollectionField) composite.lookupChild("dogs");
    assertNotNull(collection.lookupChild("title"));
    assertEquals("rich_doc", collection.getTypeMmd().getName());

    // bar.tables
    collection = (MetaMetadataCollectionField) getNestedField(repo, "bar", "tables");
    assertNotNull(collection.lookupChild("location"));
    assertEquals("document", collection.getTypeMmd().getName());

    // bar.chairs
    collection = (MetaMetadataCollectionField) getNestedField(repo, "bar", "chairs");
    assertNotNull(collection.lookupChild("title"));
    assertEquals("rich_doc", collection.getTypeMmd().getName());

    // qux.baz
    composite = (MetaMetadataCompositeField) getNestedField(repo, "qux", "baz");
    assertNotNull(composite.lookupChild("cats"));
    assertNotNull(composite.lookupChild("dogs"));
    assertEquals("foo", composite.getTypeMmd().getName());

    // qux.baz.cats
    collection = (MetaMetadataCollectionField) composite.lookupChild("cats");
    assertNotNull(collection);
    System.out.println(composite);
    System.out.println(collection);
    assertNotNull(collection.lookupChild("title"));
    assertEquals("rich_doc", collection.getTypeMmd().getName());

    // qux.baz.dogs
    collection = (MetaMetadataCollectionField) composite.lookupChild("dogs");
    assertNotNull(collection);
    assertNotNull(collection.lookupChild("images"));
    assertEquals("richer_doc", collection.getTypeMmd().getName());

    // qux.tables
    collection = (MetaMetadataCollectionField) getNestedField(repo, "qux", "tables");
    assertNotNull(collection.lookupChild("title"));
    assertEquals("rich_doc", collection.getTypeMmd().getName());

    // qux.chairs
    collection = (MetaMetadataCollectionField) getNestedField(repo, "qux", "chairs");
    assertNotNull(collection.lookupChild("images"));
    assertEquals("richer_doc", collection.getTypeMmd().getName());
  }

}
