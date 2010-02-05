/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.util.ArrayList;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.generated.library.WeatherReport;

/**
 * This is a listener class in order to collect weather reports.
 * 
 * @author quyin
 */
public class WeatherReportCollector implements MyContainer.MetadataCollectingListener
{
	/**
	 * Make it a singleton.
	 */
	protected WeatherReportCollector()
	{
		list = new ArrayList<WeatherReport>();
	}

	private static WeatherReportCollector	instance;

	/**
	 * Get the global singleton instance of this class.
	 * @return The global singleton instance of WeatherReportCollector.
	 */
	public static WeatherReportCollector get()
	{
		if (instance == null)
			instance = new WeatherReportCollector();
		return instance;
	}

	private ArrayList<WeatherReport>	list;

	/**
	 * Get the list of collected weather reports.
	 * @return
	 */
	public ArrayList<WeatherReport> list()
	{
		return list;
	}

	/**
	 * The actual collecting method.
	 */
	public void collect(Metadata metadata)
	{
		// check validity.
		if (metadata == null || !(metadata instanceof WeatherReport))
			return;

		WeatherReport weatherReport = (WeatherReport) metadata;
		// if there is parsing error, we don't collect it.
		if (weatherReport.city().getValue() == null)
			return;

		list.add(weatherReport);
	}
}
