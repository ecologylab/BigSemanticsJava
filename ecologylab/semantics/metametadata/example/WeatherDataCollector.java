/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.metametadata.example.generated.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metametadata.example.generated.WeatherReport;
import ecologylab.xml.SIMPLTranslationException;

/**
 * This example shows how to use a search as seed to collect data from the Internet.
 * 
 * We start by a google search of weather in Texas, then parse the search result and collect data
 * with meta-metadata library.
 * 
 * @author quyin
 */
public class WeatherDataCollector
{
	static List<WeatherReport> collected = new ArrayList<WeatherReport>();
	
	/**
	 * Before you write your own codes, make sure that use the VM arguments like this project Or the
	 * DownloadMonitor will never start downloading due to limited available memory!
	 * 
	 * @param args
	 * @throws SIMPLTranslationException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws SIMPLTranslationException, IOException,
			InterruptedException
	{
		// register our own semantic action
		SemanticAction.register(SaveReportSemanticAction.class);
		// create the infoCollector
		// the repository files are in a sub-directory, indicated by the first parameter
		// the metadata translation scope (generated by the compiler) is indicated by the second parameter
		// !! note that here the GeneratedMetadataTranslationScope is in the package
		//    ecologylab.semantics.metametadata.example.generated, not the one used by cF
		MyInfoCollector infoCollector = new MyInfoCollector("repo", GeneratedMetadataTranslationScope.get());
		
		// seeding start url
		ParsedURL seedUrl = ParsedURL
				.getAbsolute("http://www.google.com/search?q=texas+site%3Awww.wunderground.com");
		infoCollector.getContainerDownloadIfNeeded(null, seedUrl, null, false, false, false);
		// TODO detect ending of downloading and parsing
		Thread.sleep(30000);
		infoCollector.getDownloadMonitor().stop();
		
		// output to a .csv
		OutputStream outs = new FileOutputStream("output.csv");
		PrintWriter writer = new PrintWriter(outs);
		writer.printf("#format:city,weather,picture_url,temperature,humidity,wind_speed,dewpoint\n");
		for (WeatherReport report : collected)
		{
				//check to make sure there are no parsing error
				if (report.getCity() != null)
				{				
					writer.printf(
						"%s,%s,%s,%s,%s,%s,%s\n",
						report.getCity().split(",")[0],
						report.getWeather(),
						report.getPicUrl(),
						report.getTemperature(),
						report.getHumidity(),
						report.getWind(),
						report.getDewPoint()
						);
				}
		}
		writer.flush();
		writer.close();
		outs.close();
	}

}
