package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ecologylab.bigsemantics.BaseMmdTest;
import ecologylab.serialization.SIMPLTranslationException;

/**
 * Regression tests for inheritance issues.
 * 
 * @author quyin
 */
public class TestInheritanceIssues extends BaseMmdTest
{

  @Test
  public void testTitleLabelIssue() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritanceIssue1.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadata mmd = repo.getMMByName("document");
    serializeToTempFile(mmd, "1");
    MetaMetadataField titleField = getNestedField(mmd, "title");
    assertEquals("title", titleField.getLabel());
  }

  @Test
  public void testScalarCollectionIssue() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritanceIssue2.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadataField field = null;

    // creative_work related:

    field = getNestedField(repo, "creative_work", "authors");
    assertEquals(1, field.getXpaths().size());
    assertEquals("//meta[@name='author']/@content", field.getXpath(0));

    field = getNestedField(repo, "creative_work", "keywords");
    assertEquals(1, field.getXpaths().size());
    assertEquals("//meta[@name='keyword']/@content", field.getXpath(0));

    // book immediate kids:

    field = getNestedField(repo, "book", "authors");
    System.out.println(field.getXpaths());
    assertEquals(2, field.getXpaths().size());
    assertEquals("//div[@class='author']/a", field.getXpath(0));
    assertEquals("//meta[@name='author']/@content", field.getXpath(1));

    field = getNestedField(repo, "book", "keywords");
    assertEquals(1, field.getXpaths().size());
    assertEquals("//meta[@name='keyword']/@content", field.getXpath(0));

    field = getNestedField(repo, "book", "tags");
    assertEquals(1, field.getXpaths().size());
    assertEquals("//div[@class='tag']/a", field.getXpath(0));

    // book.related_book kids:

    field = getNestedField(repo, "book", "related_books", "authors");
    assertEquals(3, field.getXpaths().size());
    assertEquals("//div[@id='related']//div[@class='author']/a", field.getXpath(0));
    assertEquals("//div[@class='author']/a", field.getXpath(1));
    assertEquals("//meta[@name='author']/@content", field.getXpath(2));

    field = getNestedField(repo, "book", "related_books", "keywords");
    assertEquals(2, field.getXpaths().size());
    assertEquals("//div[@id='related']//div[@class='keyword']/a", field.getXpath(0));
    assertEquals("//meta[@name='keyword']/@content", field.getXpath(1));

    field = getNestedField(repo, "book", "related_books", "tags");
    assertEquals(1, field.getXpaths().size());
    assertEquals("//div[@class='tag']/a", field.getXpath(0));
  }

  @Test
  public void testInheritingFromBothTypeAndSuperFieldIssue() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritanceIssue3.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    MetaMetadataField field = null;

    field = getNestedField(repo, "document", "location");
    assertEquals(0, field.getXpathsSize());

    field = getNestedField(repo, "compound_document", "location");
    assertEquals(0, field.getXpathsSize());

    field = getNestedField(repo, "compound_document", "main_images", "location");
    assertEquals(1, field.getXpathsSize());
    assertEquals("//meta[@name='image']/@content", field.getXpath(0));

    field = getNestedField(repo, "commodity", "location");
    assertEquals(0, field.getXpathsSize());

    field = getNestedField(repo, "commodity", "main_images", "location");
    assertEquals(1, field.getXpathsSize());
    assertEquals("//meta[@name='image']/@content", field.getXpath(0));

    field = getNestedField(repo, "ikea_product", "location");
    assertEquals(1, field.getXpathsSize());
    assertEquals("//test-location", field.getXpath(0));

    field = getNestedField(repo, "ikea_product", "main_images", "location");
    assertEquals(1, field.getXpathsSize());
    assertEquals("//meta[@name='image']/@content", field.getXpath(0));

    field = getNestedField(repo, "ikea_product", "depts", "products", "location");
    assertEquals(2, field.getXpathsSize());
    assertEquals("./@href", field.getXpath(0));
    assertEquals("//test-location", field.getXpath(1));

    field = getNestedField(repo, "ikea_product", "depts", "products", "main_images", "location");
    assertEquals(2, field.getXpathsSize());
    assertEquals("./img/@src", field.getXpath(0));
    assertEquals("//meta[@name='image']/@content", field.getXpath(1));
  }

  @Test
  public void testInheritingFromUnprocessedSuperField() throws SIMPLTranslationException
  {
    MetaMetadataRepository repo = loadRepository("/testInheritanceIssue4.xml");

    InheritanceHandler handler = new InheritanceHandler();
    handler.handleMmdRepository(repo);

    // expect it does not throw an exception.
  }

}
