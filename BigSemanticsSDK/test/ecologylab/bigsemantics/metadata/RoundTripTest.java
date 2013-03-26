package ecologylab.bigsemantics.metadata;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class RoundTripTest
{
  
	static SimplTypesScope			metadataTScope	= RepositoryMetadataTranslationScope.get();
	
  static SemanticsSessionScope sss = new SemanticsSessionScope(metadataTScope, CybernekoWrapper.class);

	private Object			lockDoc					= new Object();

	Document						document;

	MetadataComparator	comparator			= new MetadataComparator();

	@Test
	public void validateMetadataRoundTrip() throws SIMPLTranslationException, InterruptedException
	{
		ParsedURL purl = ParsedURL.getAbsolute("http://dl.acm.org/citation.cfm?id=1835572");
		Document doc = getMetadata(purl);

		String xml = SimplTypesScope.serialize(doc, StringFormat.XML).toString();
		assertNotNull(xml);
		assertTrue(xml.length() > 0);
		MetadataDeserializationHookStrategy strategy = new MetadataDeserializationHookStrategy(sss);
		Document newDoc = (Document) metadataTScope.deserialize(xml, strategy, StringFormat.XML);
		
		assertTrue(comparator.compare(doc, newDoc) == 0);
	}

	private Document getMetadata(ParsedURL purl) throws InterruptedException
	{
		requestMetadata(purl);
		return this.document;
	}

	private void requestMetadata(ParsedURL purl) throws InterruptedException
	{
		document = sss.getOrConstructDocument(purl);

		DocumentClosure closure = document.getOrConstructClosure();
		closure.addContinuation(new Continuation<DocumentClosure>()
		{
			@Override
			public void callback(DocumentClosure closure)
			{
				synchronized (lockDoc)
				{
					lockDoc.notify();
					document = closure.getDocument();
				}
			}
		});
		closure.queueDownload();

		synchronized (lockDoc)
		{
			lockDoc.wait();
		}
	}
}
