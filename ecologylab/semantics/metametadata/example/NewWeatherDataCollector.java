package ecologylab.semantics.metametadata.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.w3c.tidy.Tidy;

import ecologylab.generic.Continuation;
import ecologylab.io.DownloadProcessor;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.semantics.generated.library.WeatherReport;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;

public class NewWeatherDataCollector implements Continuation<DocumentClosure>
{
	private OutputStream	out;

	public NewWeatherDataCollector(OutputStream out)
	{
		this.out = out;
	}

	public void collect()
	{
		// register our own semantic action
		SemanticAction.register(SaveImageSemanticAction.class);

		// create the infoCollector
		SemanticsSessionScope infoCollector = new SemanticsSessionScope(RepositoryMetadataTranslationScope.get(), Tidy.class);

		// seeding start url
		String seedUrlStr = "http://www.google.com/search?q=texas+site%3Awww.wunderground.com";
		ParsedURL seedUrl = ParsedURL.getAbsolute(seedUrlStr);
		Document seedDoc = infoCollector.getOrConstructDocument(seedUrl);
		DocumentClosure downloadClosure = seedDoc.getOrConstructClosure();
		downloadClosure.addContinuation(this);
		downloadClosure.queueDownload();

		// request the download monitor to stop (after all the downloads are handled),
		// so we can exit gracefully.
		DownloadProcessor downloadMonitor = downloadClosure.downloadMonitor();
		downloadMonitor.requestStop();
	}

	@Override
	public void callback(DocumentClosure o)
	{
		Document doc = o.getDocument();
		if (doc != null && doc instanceof WeatherReport)
		{
			WeatherReport report = (WeatherReport) doc;
			if (report.getCity() != null)
			{
				synchronized (out)
				{
					String s = String.format(
							"%s,%s,%s,%s,%s,%s\n",
							report.getCity(),
							report.getWeather(),
							report.getTemperature(),
							report.getHumidity(),
							report.getWind(),
							report.getDewPoint()
							);
					try
					{
						out.write(s.getBytes("utf-8"));
						out.flush();
					}
					catch (UnsupportedEncodingException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws UnsupportedEncodingException, IOException
	{
		// prepare output file (it will be closed by the system after program stops)
		OutputStream out = new FileOutputStream(new File("output.csv"));
		out.write("city,weather,temperature,humidity,wind_speed,dewpoint\n".getBytes("utf-8"));
		NewWeatherDataCollector nwdc = new NewWeatherDataCollector(out);
		nwdc.collect();
	}

}
