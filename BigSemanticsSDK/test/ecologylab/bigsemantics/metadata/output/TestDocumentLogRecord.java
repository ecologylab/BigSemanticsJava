package ecologylab.bigsemantics.metadata.output;

import org.junit.Assert;
import org.junit.Test;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SimplTypesScope;

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
	    
	    Assert.assertTrue(logRecord.getmSecInExtraction() > 0);
	    Assert.assertTrue(logRecord.getmSecInHtmlDownload() > 0);
	    Assert.assertTrue(logRecord.getEnQueueTimestamp() > 0);
	}

}
