package ecologylab.bigsemantics.metadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.generated.library.person.author.*;
// import ecologylab.bigsemantics.generated.library.curated.CurationRecord;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

public class TestCuratedMixins extends Assert{

	/*
	CurationRecord curationRecord;
	
	CurationRecord generateCurationRecord()
	{
		CurationRecord curationRecord = new CurationRecord();
		Author user = new  Author();
		user.setTitle("rhema");
		curationRecord.setCurator(user);
		curationRecord.setLastModified(new Date());
		ArrayList<MetadataString> tags = new ArrayList<MetadataString>();
		tags.add(new MetadataString("time"));
		tags.add(new MetadataString("love"));
		tags.add(new MetadataString("social"));
		tags.add(new MetadataString("money"));
		curationRecord.setTags(tags);
		return curationRecord;
	}
	
	@Before
	public void Setup()
	{
		curationRecord = generateCurationRecord();
	}
	
	@Test
	public void TestDeSerializeOfCurated() throws SIMPLTranslationException
	{
		SimplTypesScope.serialize(curationRecord, System.out, Format.JSON);
	}
	
	@Test
	public void CuratedInMixinOfMetadata() throws MalformedURLException, SIMPLTranslationException
	{
		Document d = new Document();
		d.setLocation(new ParsedURL(new URL("http://google.com")));
		d.setTitle("Google.com.  The search engine you have probably heard of.");
		ArrayList<Metadata> mixins = new ArrayList<Metadata>();
		mixins.add(generateCurationRecord());
		mixins.add(generateCurationRecord());
		mixins.add(generateCurationRecord());
		mixins.add(generateCurationRecord());
		mixins.add(generateCurationRecord());
		d.setMixins(mixins);
		SimplTypesScope.serialize(d, System.out, Format.JSON);
	}
	*/
	
}
