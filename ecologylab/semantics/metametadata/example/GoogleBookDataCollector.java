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
import ecologylab.semantics.generated.library.GoogleBook;
import ecologylab.semantics.metametadata.MetaMetadataRepository;

public class GoogleBookDataCollector implements DispatchTarget<MyContainer>
{
	static List<GoogleBook>	collected	= new ArrayList<GoogleBook>();

	private int									processed	= 0;
	
	/**
	 * This example shows how to use the library to collect information and perform semantic actions
	 * on it.
	 * 
	 * @throws InterruptedException
	 * @throws FileNotFoundException
	 */
	public void collect() throws InterruptedException, FileNotFoundException
	{
		// register our own semantic action
		SemanticAction.register(SaveImageSemanticAction.class);

		// create the infoCollector
		MetaMetadataRepository repository = MetaMetadataRepository.load(new File(
				"../ecologylabSemantics/repository"));
		SemanticActionHandlerFactory semanticActionHandlerFactory = new SemanticActionHandlerFactory()
		{
			@Override
			public SemanticActionHandler create()
			{
				return new MySemanticActionHandler();
			}
		};
		MyInfoCollector infoCollector = new MyInfoCollector<MyContainer>(repository,
			GeneratedMetadataTranslationScope.get(), semanticActionHandlerFactory, 1);

		// seeding start url
		ParsedURL seedUrl = ParsedURL
				.getAbsolute("http://books.google.com/books?id=fu5HtixRje8C");
		infoCollector.getContainerDownloadIfNeeded(null, seedUrl, null, false, false, false, this);

		while (processed<1)
		{
			Thread.sleep(1000);	
		}
		Thread.sleep(3000); // allow some time for some processing
		infoCollector.getDownloadMonitor().stop(); // stop downloading thread(s).

		for (GoogleBook book : collected)
		{

			System.out.println("Title: " + book.getTitle());
			System.out.println("Category: " + book.getCategory());
			System.out.println("About the author: " + book.getAboutTheAuthor());
			System.out.println("Description: " + book.getDescription());
			System.out.println("ISBN: " + book.getIsbn());
			System.out.println("Publisher: " + book.getPublisher());
		}

	}


public static void main(String[] args) throws FileNotFoundException, InterruptedException
{
	GoogleBookDataCollector gbdc = new GoogleBookDataCollector();
	gbdc.collect();
}

/**
 * This is the callback for dispatching. It will be invoked from DownloadMonitor to notify that a
 * container is already downloaded and processed.
 * 
 * @see DownloadMonitor
 */

@Override
public void delivery(MyContainer o)
{
	processed++;
}

}
