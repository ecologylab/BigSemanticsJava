/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.util.ArrayList;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.generated.library.WeatherReport;

/**
 * @author quyin
 * 
 */
public class WeatherReportCollector implements MyContainer.MetadataCollectingListener
{
	protected WeatherReportCollector()
	{
		list = new ArrayList<WeatherReport>();
	}

	private static WeatherReportCollector	instance;

	public static WeatherReportCollector get()
	{
		if (instance == null)
			instance = new WeatherReportCollector();
		return instance;
	}

	private ArrayList<WeatherReport>	list;

	public ArrayList<WeatherReport> list()
	{
		return list;
	}

	public void collect(Metadata metadata)
	{
		if (metadata == null || !(metadata instanceof WeatherReport))
			return;

		WeatherReport weatherReport = (WeatherReport) metadata;
		if (weatherReport.city().getValue() == null)
			return;

		list.add((WeatherReport) metadata);
	}
}
