package ecologylab.bigsemantics.metadata;

import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
import ecologylab.bigsemantics.cyberneko.CybernekoWrapper;
import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SimplTypesScope;

public class MetadataTestHelper
{
	static SimplTypesScope				metadataTScope				= RepositoryMetadataTranslationScope.get();

	static SemanticsSessionScope	semanticSessionScope	= new SemanticsSessionScope(metadataTScope,
																													CybernekoWrapper.class);

	private Document							document;

	private Object								lockDoc								= new Object();

	public Document getMetadata(ParsedURL purl) throws InterruptedException
	{
		requestMetadata(purl);
		return this.document;
	}

	private void requestMetadata(ParsedURL purl) throws InterruptedException
	{
		document = semanticSessionScope.getOrConstructDocument(purl);

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
