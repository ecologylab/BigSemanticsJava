package ecologylab.bigsemantics.metadata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class RoundTripTest
{
	MetadataComparator	comparator	= new MetadataComparator();

	@Test
	public void validateMetadataRoundTrip() throws SIMPLTranslationException, InterruptedException
	{
		ParsedURL purl = ParsedURL.getAbsolute("http://dl.acm.org/citation.cfm?id=1835572");
		// ParsedURL purl = ParsedURL.getAbsolute("http://www.amazon.com/gp/product/B0050SYS5A/");

		MetadataTestHelper m = new MetadataTestHelper();
		Document doc = m.getMetadata(purl);

		String xml = SimplTypesScope.serialize(doc, StringFormat.XML).toString();
		assertNotNull(xml);
		assertTrue(xml.length() > 0);

		MetadataDeserializationHookStrategy strategy = new MetadataDeserializationHookStrategy(
				MetadataTestHelper.semanticSessionScope);
		SimplTypesScope	metadataTScope = MetadataTestHelper.metadataTScope;
		Document newDoc = (Document) metadataTScope.deserialize(xml, strategy, StringFormat.XML);
		assertTrue(comparator.compare(doc, newDoc) == 0);
	}
}
