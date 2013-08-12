package ecologylab.bigsemantics.metadata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * 
 * @author colton
 */
public class DeserializationTest
{
	MetadataComparator comparator = new MetadataComparator();

/* Not required to demonstrate deserialization error
	@Test
	public void validateMetadataDeserializationXML()
			throws SIMPLTranslationException, InterruptedException
	{
		ParsedURL purl = ParsedURL
				.getAbsolute("http://news.blogs.cnn.com/2011/04/14/predator-"
						+ "dinosaurs-may-have-been-night-hunters/?hpt=C2");

		MetadataTestHelper m = new MetadataTestHelper();
		Document doc = m.getMetadata(purl);

		assertNotNull(doc.getClippings());

		String xml = SimplTypesScope.serialize(doc, StringFormat.XML)
				.toString();
		assertNotNull(xml);
		assertTrue(xml.length() > 0);

		MetadataDeserializationHookStrategy strategy = new MetadataDeserializationHookStrategy(
				MetadataTestHelper.semanticSessionScope);
		SimplTypesScope metadataTScope = MetadataTestHelper.metadataTScope;
		Document newDoc = (Document) metadataTScope.deserialize(xml, strategy,
				StringFormat.XML);

		assertNotNull(doc.getClippings());
		assertTrue(comparator.compare(doc, newDoc) == 0);
	}

	@Test
	public void validateMetadataDeserialiazationJSON()
			throws SIMPLTranslationException, InterruptedException
	{
		ParsedURL purl = ParsedURL
				.getAbsolute("http://news.blogs.cnn.com/2011/04/14/predator-"
						+ "dinosaurs-may-have-been-night-hunters/?hpt=C2");

		MetadataTestHelper m = new MetadataTestHelper();
		Document doc = m.getMetadata(purl);

		assertNotNull(doc.getClippings());

		String json = SimplTypesScope.serialize(doc, StringFormat.JSON)
				.toString();

		assertNotNull(json);
		assertTrue(json.length() > 0);

		MetadataDeserializationHookStrategy strategy = new MetadataDeserializationHookStrategy(
				MetadataTestHelper.semanticSessionScope);
		SimplTypesScope metadataTScope = MetadataTestHelper.metadataTScope;
		Document newDoc = (Document) metadataTScope.deserialize(json, strategy,
				StringFormat.JSON);

		assertNotNull(doc.getClippings());
		assertTrue(comparator.compare(doc, newDoc) == 0);
	}*/

	@Test
	public void validateMetadataDeserializationPresetXML()
			throws SIMPLTranslationException, InterruptedException
	{
		String xml = "<compound_document mm_name=\"compound_document\" location=\""
				+ "http://news.blogs.cnn.com/2011/04/14/predator-dinosaurs-may-have-been-night-hunters"
				+ "/?hpt=C2\" download_status=\"DOWNLOAD_DONE\" page_structure=\"content_page\"><title>"
				+ "Predator dinosaurs may have been night-hunters </title><additional_locations>"
				+ "<location>http://news.blogs.cnn.com/2011/04/14/predator-dinosaurs-may-have-been-night-"
				+ "hunters/?hpt=C2</location></additional_locations><clippings><clipping mm_name=\""
				+ "clippings\"></clipping></clippings><root_document mm_name=\"root_document\" "
				+ "download_status=\"UNPROCESSED\"></root_document></compound_document>";

		MetadataDeserializationHookStrategy strategy = new MetadataDeserializationHookStrategy(
				MetadataTestHelper.semanticSessionScope);
		SimplTypesScope metadataTScope = MetadataTestHelper.metadataTScope;
		Document doc = (Document) metadataTScope.deserialize(xml, strategy,
				StringFormat.XML);

		assertNotNull(doc.getClippings());
	}

	//@Test
	public void validateMetadataDeserializationPresetJSON()
			throws SIMPLTranslationException, InterruptedException
	{
		String json = "{\"compound_document\": {\"mm_name\": \"compound_document\",\"location\":"
				+ " \"http://www.flickr.com/search/?q=summer&z=m\",\"download_status\": "
				+ "\"DOWNLOAD_DONE\",\"title\": \"Flickr Search: summer\",\"additional_locations\":"
				+ " {\"location\": [\"http://www.flickr.com/search/?q=summer&z=m\"]},\"clippings\": "
				+ "[{\"clipping\": {\"mm_name\": \"clippings\"}}],\"root_document\": {\"mm_name\": "
				+ "\"root_document\",\"download_status\": \"UNPROCESSED\"}}}";

		MetadataDeserializationHookStrategy strategy = new MetadataDeserializationHookStrategy(
				MetadataTestHelper.semanticSessionScope);
		SimplTypesScope metadataTScope = MetadataTestHelper.metadataTScope;
		Document doc = (Document) metadataTScope.deserialize(json, strategy,
				StringFormat.JSON);

		assertNotNull(doc.getClippings());
	}
}