package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.generated.library.WeatherReport;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.generated.library.scienceDirect.Publication;
import ecologylab.semantics.generated.library.scienceDirect.ScienceDirectArticle;
import ecologylab.semantics.generated.library.scienceDirect.ScienceDirectAuthor;
import ecologylab.semantics.generated.library.scienceDirect.ScienceDirectReference;

public class ScienceDirectDataCollector implements DispatchTarget<MyContainer>
{
	static List<ScienceDirectArticle>	collected	= new ArrayList<ScienceDirectArticle>();

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
				.getAbsolute("http://www.sciencedirect.com/science?_ob=ArticleURL&_udi=B6WGR-45F4P3T-D&_user=952835&_coverDate=11/30/2000&_rdoc=3&_fmt=high&_orig=browse&_origin=browse&_zone=rslt_list_item&_srch=doc-info%28%23toc%236829%232000%23999469994%23293893%23FLP%23display%23Volume%29&_cdi=6829&_sort=d&_docanchor=&view=c&_ct=11&_acct=C000049198&_version=1&_urlVersion=0&_userid=952835&md5=7561c89aecdf2b924da6b11bc37bc1a9&searchtype=a");
		infoCollector.getContainerDownloadIfNeeded(null, seedUrl, null, false, false, false, this);
		// when a wunderground.com page is processed, field 'processed' will be increased by 1 by the
		// download monitor. so we count this field to see if we have reached our goal: 5 results.
		while (processed<1)
		{
			Thread.sleep(1000);	
		}
		Thread.sleep(3000); // allow some time for some processing
		infoCollector.getDownloadMonitor().stop(); // stop downloading thread(s).
		// output to a .csv
		//PrintWriter writer = new PrintWriter(new FileOutputStream("sd_output.csv"));
		//writer.printf("#format:title,abstract,publication name,volume,issue,date,pages,copyright,doi,url,recieved,accepted,online,author name,author affiliation,author contact,\n");
		//writer.printf("title\n");
		for (ScienceDirectArticle article : collected)
		{
			/*
			// check to make sure there are no parsing error
			if (article.getTitle() != null)
			{
				writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", article.getTitle(), article.getAbst(), article.getPublication().getPublicationName(),
						article.getPublication().getVolume().toString(), article.getPublication().getIssue().toString(), article.getPublication().getDate(),
						article.getPublication().getPages(), article.getPublication().getCopyright(), article.getPublication().getDoi(), article.getUrl(), article.getRecievedDate(),
						article.getAcceptedDate(), article.getOnlineDate());
				for (ScienceDirectAuthor author : article.getAuthors())
				{
					writer.printf("%s,%s,%s,%s\n", author.getName(), author.getAffiliation(), author.getContact());
				}
			}*/
			System.out.println("Title: " + article.getTitle());
			System.out.println("Abstract: " + article.getAbstractField());
			System.out.println("Volume: " + article.getVolume());
			System.out.println("Issue: " + article.getIssue());
			System.out.println("Pages: " + article.getPages());
			//System.out.println("Date: " + article.getDate());
			System.out.println("Publication Name: " + article.getPublicationName());
			System.out.println("DOI: " + article.getDoi());
			System.out.println("Recieved: " + article.getRecieved());
			System.out.println("Accepted: " + article.getAccepted());
			System.out.println("Online: " + article.getOnline());
		}
		//writer.close();
	}


public static void main(String[] args) throws FileNotFoundException, InterruptedException
{
	ScienceDirectDataCollector sddc = new ScienceDirectDataCollector();
	sddc.collect();
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
