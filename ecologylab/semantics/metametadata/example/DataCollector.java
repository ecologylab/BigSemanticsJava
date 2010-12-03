package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import ecologylab.generic.Debug;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.SIMPLTranslationException;

public class DataCollector extends Debug implements DispatchTarget<MyContainer>
{
	List<Metadata>	collected						= new ArrayList<Metadata>();

	Object					downloadMonitorLock	= new Object();

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
			ParsedURL seedUrl = ParsedURL.getAbsolute(urls[i]);
			infoCollector.getContainerDownloadIfNeeded(null, seedUrl, null, false, false, false, this);
		}

		// wait for the work to be finished
		while (!infoCollector.getDownloadMonitor().isIdle())
		{
			synchronized (downloadMonitorLock)
			{
				try
				{
					downloadMonitorLock.wait(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		// allow some time for possible post-processing
		try
		{
			Thread.sleep(3000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		// stop download thread(s)
		infoCollector.getDownloadMonitor().stop();

		// output results
		System.out.print("\n");
		synchronized (collected)
		{
			for (Metadata m : collected)
			{
				try
				{
					m.serialize(System.out);
					System.out.println();
				}
				catch (SIMPLTranslationException e)
				{
					e.printStackTrace();
				}
				System.out.print('\n');
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException, InterruptedException
	{
		DataCollector dc = new DataCollector();
		dc.collect(args);
	}

	@Override
	public void delivery(MyContainer container)
	{
		Metadata metadata = container.metadata();
		if (metadata == null)
		{
			warning("null metadata for container " + container);
			return;
		}
		synchronized (collected)
		{
			collected.add(metadata);
		}
		synchronized (downloadMonitorLock)
		{
			downloadMonitorLock.notify();
		}
	}

}
