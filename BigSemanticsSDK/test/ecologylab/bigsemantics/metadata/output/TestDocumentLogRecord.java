package ecologylab.bigsemantics.metadata.output;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTypesScope;
import ecologylab.bigsemantics.logging.DocumentLogRecord;
import ecologylab.bigsemantics.logging.Phase;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class TestDocumentLogRecord
{

	private Object	lockDoc	= new Object();

	@Test
	public void testTimeIntervalValues() throws InterruptedException
	{
		DocumentLogRecord logRecord = new DocumentLogRecord();

		SimplTypesScope metadataTScope = RepositoryMetadataTypesScope.get();
		SemanticsSessionScope sss = new SemanticsSessionScope(metadataTScope, CybernekoWrapper.class);
		ParsedURL purl = ParsedURL.getAbsolute("http://dl.acm.org/citation.cfm?id=1835572");
		Document doc = sss.getOrConstructDocument(purl);
		doc.setLogRecord(logRecord);
		DocumentClosure closure = doc.getOrConstructClosure();
		closure.addContinuation(new Continuation<DocumentClosure>()
		{
			@Override
			public void callback(DocumentClosure closure)
			{
				synchronized (lockDoc)
				{
					lockDoc.notify();
				}
			}
		});
		closure.queueDownload();
		synchronized (lockDoc)
		{
			lockDoc.wait();
		}

		Assert.assertTrue(logRecord.getTotalMs(Phase.EXTRACT) > 0);
		Assert.assertTrue(logRecord.getTotalMs(Phase.DOWNLOAD) > 0);
	}

	private String serializeToJson(Object myObject) throws SIMPLTranslationException
	{
		return SimplTypesScope.serialize(myObject, StringFormat.JSON).toString();
	}

	@Test
	public void testDeserializeDownloadableLogRecord() throws SIMPLTranslationException
	{
		DownloadableLogRecord logRecord = new DownloadableLogRecord();
		
//		logRecord.setHtmlCacheHit(false);
		logRecord.setId("eqwewqewqe");
		ArrayList<Long> peekIntervals = new ArrayList<Long>();
		peekIntervals.add(100L);
		peekIntervals.add(1000L);

		String json = serializeToJson(logRecord);
		Assert.assertNotNull(json);
		Assert.assertTrue(json.length() > 0);

		SimplTypesScope tscope = SimplTypesScope.get("test-deserializing-downloadable-log-record",
				DownloadableLogRecord.class);
		logRecord = (DownloadableLogRecord) tscope.deserialize(json, StringFormat.JSON);
		
//		Assert.assertFalse(logRecord.isHtmlCacheHit());
		Assert.assertTrue(logRecord.getId().equals("eqwewqewqe"));
	}

}
