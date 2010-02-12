/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.util.ArrayList;
import ecologylab.semantics.metadata.Metadata;

/**
 * This is a listener class in order to collect metadata.
 * 
 * @author quyin
 */
public class MetadataCollector implements MyContainer.MetadataCollectingListener
{
	/**
	 * Make it a singleton.
	 */
	protected MetadataCollector()
	{
		list = new ArrayList<Metadata>();
	}

	private static MetadataCollector	instance;

	/**
	 * Get the global singleton instance of this class.
	 * @return The global singleton instance of WeatherReportCollector.
	 */
	public static MetadataCollector get()
	{
		if (instance == null)
			instance = new MetadataCollector();
		return instance;
	}

	private ArrayList<Metadata>	list;

	/**
	 * Get the list of collected weather reports.
	 * @return
	 */
	public ArrayList<Metadata> list()
	{
		return list;
	}

	/**
	 * The actual collecting method.
	 */
	public void collect(Metadata metadata)
	{
		// check validity.
		if (metadata == null)
			return;

		list.add(metadata);
	}
}
