package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

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

	PrintStream	out;

	Object			downloadMonitorLock	= new Object();

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

		// prepare output file (it will be closed by the system after program stops)
		out = new PrintStream(new File("output.csv"));
		out.println("city,weather,temperature,humidity,wind_speed,dewpoint");

		// seeding start url
		ParsedURL seedUrl = ParsedURL
				.getAbsolute("http://www.google.com/search?q=texas+site%3Awww.wunderground.com");
		infoCollector.getContainerDownloadIfNeeded(null, seedUrl, null, false, false, false, this);

		// request the download monitor to stop (after all the downloads are handled) so we can exit
		// gracefully.
		infoCollector.getDownloadMonitor().requestStop();
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
		if (metadata != null && metadata instanceof WeatherReport)
		{
			WeatherReport report = (WeatherReport) metadata;
			if (report.getCity() != null)
			{
				synchronized (out)
				{
					out.format("%s,%s,%s,%s,%s,%s", report.getCity(), report.getWeather(),
							report.getTemperature(), report.getHumidity(), report.getWind(), report.getDewPoint());
					out.println();
					out.flush();
				}
			}
		}
	}

}
