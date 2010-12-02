package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.SIMPLTranslationException;

public class DataCollector implements DispatchTarget<MyContainer>
{
	static List<Metadata>	collected	= new ArrayList<Metadata>();

	private int						processed	= 0;

	public void collect(String[] urls) throws InterruptedException, FileNotFoundException
	{

			// register our own semantic action
			SemanticAction.register(SaveImageSemanticAction.class);

			// create the infoCollector
			MetaMetadataRepository repository = MetaMetadataRepository.load(new File(
			"../ecologylabSemantics/repository"));
		for (int i = 0;i<urls.length;i++)
		{

			MyInfoCollector infoCollector = new MyInfoCollector<MyContainer>(repository,
					GeneratedMetadataTranslationScope.get(), 1);

			// seeding start url
			ParsedURL seedUrl = ParsedURL
			.getAbsolute(urls[i]);
			infoCollector.getContainerDownloadIfNeeded(null, seedUrl, null, false, false, false, this);
			while (processed<1)
			{
				Thread.sleep(1000);	
			}
			Thread.sleep(3000); // allow some time for some processing
			infoCollector.getDownloadMonitor().stop(); // stop downloading thread(s).
		}
		System.out.print("\n");
			for(Metadata m : collected)
			{
				System.out.print(m.getClassName() + ": ");
				try
				{
					m.serialize(System.out);
				}
				catch (SIMPLTranslationException e)
				{
					e.printStackTrace();
				}
				System.out.print('\n');
			}

	}

	public static void main(String[] args) throws FileNotFoundException, InterruptedException
	{
		DataCollector dc = new DataCollector();
		dc.collect(args);
	}

	@Override
	public void delivery(MyContainer o)
	{
		processed++;
	}
}
