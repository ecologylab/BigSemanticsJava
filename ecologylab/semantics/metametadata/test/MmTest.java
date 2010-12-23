package ecologylab.semantics.metametadata.test;

import java.io.File;
import java.io.FileNotFoundException;

import ecologylab.generic.Debug;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.example.MyContainer;
import ecologylab.semantics.metametadata.example.MyInfoCollector;
import ecologylab.serialization.SIMPLTranslationException;

/**
 * Test program for meta-metadata curator / authors.
 * Takes URLs to extract information from as arguments.
 * Uses meta-metadata repository to identify appropriate wrapper / type.
 * Prints results of information extraction to the console.
 */
public class MmTest extends Debug implements DispatchTarget<MyContainer>
{

	Object	outputLock	= new Object();

	public void collect(String[] urls)
	{
		// create the infoCollector
		MetaMetadataRepository repository = MetaMetadataRepository.load(new File(
				"../ecologylabSemantics/repository"));
		MyInfoCollector<MyContainer> infoCollector = new MyInfoCollector<MyContainer>(repository,
				GeneratedMetadataTranslationScope.get(), 1);

		// seed start urls
		for (int i = 0; i < urls.length; i++)
		{
			if ("//".equals(urls[i]))
			{
				System.err.println("Terminate due to //");
				break;
			}
			ParsedURL seedUrl = ParsedURL.getAbsolute(urls[i]);
			infoCollector.getContainerDownloadIfNeeded(null, seedUrl, null, false, false, false, this);
		}
		infoCollector.getDownloadMonitor().requestStop();
	}

	public static void main(String[] args) throws FileNotFoundException, InterruptedException
	{
		MmTest dc = new MmTest();
		dc.collect(args);
	}

	@Override
	public void delivery(MyContainer container)
	{
		Metadata metadata = container.getMetadata();
		if (metadata == null)
		{
			warning("null metadata for container " + container);
			return;
		}
		synchronized (outputLock)
		{
			try
			{
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println("Parsed Meta-Metadata:");
				System.out.print("\t");
				metadata.serialize(System.out);
				System.out.println();
				System.out.println();
				System.out.println();
			}
			catch (SIMPLTranslationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
