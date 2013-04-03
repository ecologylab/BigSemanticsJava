package ecologylab.bigsemantics.deserialization;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.generated.library.yelp.YelpBusiness;
import ecologylab.bigsemantics.metadata.MetadataDeserializationHookStrategy;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;

public class MetadataRoundTripTest {

	static SemanticsSessionScope sss;

	static {
		sss = new SemanticsSessionScope(
				RepositoryMetadataTranslationScope.get(),
				CybernekoWrapper.class);
	}

	public String serializationHelper(Object obj)
			throws SIMPLTranslationException {
		String xml = SimplTypesScope.serialize(obj, StringFormat.XML)
				.toString();

		File tempDir = PropertiesAndDirectories.tempDir();
		File tempFile = new File(tempDir, "metadata-roundtrip.xml");
		SimplTypesScope.serialize(obj, tempFile, Format.XML);
		System.out.println("metadata serialized to " + tempFile);

		return xml;
	}

	public Document deserializationHelper(String xml)
			throws SIMPLTranslationException {
		Document doc = (Document) RepositoryMetadataTranslationScope.get()
				.deserialize(xml, new MetadataDeserializationHookStrategy(sss),
						StringFormat.XML);
		assertNotNull(doc);
		assertNotNull(
				"deserialized metadata doesn't have meta-metadata! it is critical for it to have a correct meta-metadata for deserialization of the whole object.",
				doc.getMetaMetadata());
		return doc;
	}

	private void checkYelpBusiness(YelpBusiness yb) {
		assertNotNull(yb);
		assertNotNull(yb.getTitle());
		assertNotNull(yb.getLocation());
		assertNotNull(yb.getOverallRating());
		assertNotNull(yb.getReviews());
		assertNotNull(yb.getBusinessAddress());
		assertNotNull(yb.getBusinessWebsite());
	}

	@Test
	public void testYelpBusiness() throws SIMPLTranslationException {
		ParsedURL purl = ParsedURL
				.getAbsolute("http://www.yelp.com/biz/kokkari-estiatorio-san-francisco");
		YelpBusiness yb = (YelpBusiness) MetadataHelper.downloadDocument(purl,
				sss);
		checkYelpBusiness(yb);

		String xml = serializationHelper(yb);

		YelpBusiness yb2 = (YelpBusiness) deserializationHelper(xml);
		checkYelpBusiness(yb2);
	}
}
