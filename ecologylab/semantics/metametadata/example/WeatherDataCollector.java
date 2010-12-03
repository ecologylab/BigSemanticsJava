package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ecologylab.generic.Debug;
import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.generated.library.WeatherReport;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;

/*
 * Before you write your own codes, make sure that use the VM arguments like this project Or the
 * DownloadMonitor will never start downloading due to limited available memory!
 * 
 * recommended VM arguments: -Xms128m -Xmx512m
 */

/**
 * This example shows how to use a search as seed to collect data from the Internet.
 * 
 * We start by a google search of weather in Texas, then parse the search result and collect data
 * with meta-metadata library.
 * 
 * @author quyin
 */
public class WeatherDataCollector extends Debug implements DispatchTarget<MyContainer>
{
	
	private static final long	MAX_WAIT						= 30000;

	List<WeatherReport>				collected						= new ArrayList<WeatherReport>();

	Object										downloadMonitorLock	= new Object();

	/**
	 * This example shows how to use the library to collect information and perform semantic actions
	 * on it.
	 * 
	 * @throws FileNotFoundException
	 */
	public void collect() throws FileNotFoundException
	{
		// register our own semantic action
		SemanticAction.register(SaveImageSemanticAction.class);

		// create the infoCollector
		MetaMetadataRepository repository = MetaMetadataRepository.load(new File(
				"../ecologylabSemantics/repository"));
		MyInfoCollector infoCollector = new MyInfoCollector<MyContainer>(repository,
				GeneratedMetadataTranslationScope.get(), 1);

		// seeding start url
		ParsedURL seedUrl = ParsedURL
				.getAbsolute("http://www.google.com/search?q=texas+site%3Awww.wunderground.com");
		infoCollector.getContainerDownloadIfNeeded(null, seedUrl, null, false, false, false, this);

		// when a wunderground.com page is processed, field 'processed' will be increased by 1 by the
		// download monitor. so we count this field to see if we have reached our goal: 5 results.
		while (!infoCollector.getDownloadMonitor().isIdle())
		{
			synchronized (downloadMonitorLock)
			{
				try
				{
					downloadMonitorLock.wait(MAX_WAIT);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		infoCollector.getDownloadMonitor().stop(); // stop downloading thread(s).

		// output to a .csv
		PrintWriter writer = new PrintWriter(new FileOutputStream("output.csv"));
		writer.printf("#format:city,weather,temperature,humidity,wind_speed,dewpoint\n");
		for (WeatherReport report : collected)
		{
			// check to make sure there are no parsing error
			if (report.getCity() != null)
			{
				writer.printf("%s,%s,%s,%s,%s,%s\n", report.getCity(), report.getWeather(),
						report.getTemperature(), report.getHumidity(), report.getWind(), report.getDewPoint());
			}
		}
		writer.close();
	}

	public static void main(String[] args) throws FileNotFoundException
	{
		WeatherDataCollector wdc = new WeatherDataCollector();
		wdc.collect();
	}

	/**
	 * This is the callback for dispatching. It will be invoked from DownloadMonitor to notify that a
	 * container is already downloaded and parsed.
	 * 
	 * @see DownloadMonitor
	 */
	@Override
	public void delivery(MyContainer container)
	{
		Metadata metadata = container.metadata();
		if (metadata == null)
		{
			warning("null metadata for container " + container);
			return;
		}
		if (!(metadata instanceof WeatherReport))
		{
			warning("non-weather report metadata collected: " + metadata);
			return;
		}

		synchronized (collected)
		{
			collected.add((WeatherReport) metadata);
		}
		
		synchronized (downloadMonitorLock)
		{
			downloadMonitorLock.notify();
		}
	}

}
