/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import ecologylab.collections.Scope;
import ecologylab.documenttypes.DocumentParser;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.WeatherReport;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;

/**
 * @author quyin
 *
 * This example shows how to use a search as seed to collect data from the Internet.
 * 
 * We start by a google search of weather in Texas, then parse the search result and
 * collect data with help of meta-metadata library.
 */
public class TestMetaMetadataRuntime {
	/**
	 * @param args
	 * @throws XMLTranslationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws XMLTranslationException, IOException {
		// create components
		// meta-metadata of WeatherReport have been added into the repository
		MyInfoCollector infoCollector = new MyInfoCollector(
				MetadataCompiler.DEFAULT_REPOSITORY_FILEPATH);
		
		// seeding and performing downloads
		ParsedURL seedUrl = ParsedURL.getAbsolute("http://www.google.com/search?q=texas+site%3Awww.wunderground.com");
		MyContainer container = new MyContainer(null, infoCollector, seedUrl);
		container.performDownload();
		
		// output collected data into a .csv file
		OutputStream outs = new FileOutputStream("output.csv");
		PrintWriter writer = new PrintWriter(outs);
		writer.printf("#format:city,weather,picture_url,temperature,humidity,wind_speed\n");
		for (Object element : ResultCollector.get().list())
		{
			if (element instanceof WeatherReport)
			{
				WeatherReport report = (WeatherReport) element;
				writer.printf(
						"%s,%s,%s,%s,%s,%s\n",
						report.city().getValue().split(",")[0],
						report.weather().getValue(),
						report.picUrl().getValue(),
						report.temperature().getValue(),
						report.humidity().getValue(),
						report.wind().getValue()
						);
			}
		}
		writer.flush();
		writer.close();
		outs.close();
	}

}
