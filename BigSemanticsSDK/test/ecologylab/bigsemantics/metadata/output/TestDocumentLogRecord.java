package ecologylab.bigsemantics.metadata.output;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class TestDocumentLogRecord {
	
	private Object lockDoc = new Object();
	
	@Test
	public void testTimeIntervalValues() throws InterruptedException
	{
	    DocumentLogRecord logRecord = new DocumentLogRecord();
	    
	    SimplTypesScope metadataTScope = RepositoryMetadataTranslationScope.get();
		SemanticsSessionScope sss = new SemanticsSessionScope(metadataTScope,
				                                              CybernekoWrapper.class);
		ParsedURL purl = ParsedURL.getAbsolute("http://dl.acm.org/citation.cfm?id=1835572");
	    Document doc = sss.getOrConstructDocument(purl);
	    DocumentClosure closure = doc.getOrConstructClosure();
	    closure.setLogRecord(logRecord);
	    closure.addContinuation(new Continuation<DocumentClosure>() {
			@Override
			public void callback(DocumentClosure closure) {
			    synchronized(lockDoc)
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
	    
	    Assert.assertNotNull(logRecord.getQueuePeekIntervals());
	    Assert.assertTrue(logRecord.getQueuePeekIntervals().size() > 0);
	    
	    Assert.assertTrue(logRecord.getmSecInExtraction() > 0);
	    Assert.assertTrue(logRecord.getmSecInHtmlDownload() > 0);
	    Assert.assertTrue(logRecord.getEnQueueTimestamp() > 0);
	}
	
	@Test
  public void testDeserializeDownloadableLogRecord() throws SIMPLTranslationException
  {
  	DownloadableLogRecord logRecord = new DownloadableLogRecord();
  	ArrayList<Long> peekIntervals = new ArrayList<Long>();
  	peekIntervals.add(100L);
  	peekIntervals.add(1000L);
		logRecord.setQueuePeekIntervals(peekIntervals);
		
		String json = SimplTypesScope.serialize(logRecord, StringFormat.JSON).toString();
		System.out.println(json);
		
		SimplTypesScope tscope = SimplTypesScope.get("test-deserializing-downloadable-log-record",
				DownloadableLogRecord.class);
		logRecord = (DownloadableLogRecord) tscope.deserialize(json, StringFormat.JSON);
		Assert.assertNotNull(logRecord.getQueuePeekIntervals());
    Assert.assertTrue(logRecord.getQueuePeekIntervals().size() > 0);
  }

}
