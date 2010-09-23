package ecologylab.semantics.metametadata.example;

import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.generated.library.WeatherReport;
import ecologylab.semantics.metadata.Metadata;

/**
 * This semantic handler is used for this example.
 * 
 * @author quyin
 * 
 */
public class MySemanticActionHandler extends SemanticActionHandler
{

	/**
	 * postSemanticActionsHook is invoked for every parsed metadata, after all the semantic actions
	 * are performed.
	 */
	@Override
	public void postSemanticActionsHook(Metadata metadata)
	{
		if (metadata instanceof WeatherReport)
		{
			WeatherReport wr = (WeatherReport) metadata;
			if (wr.getCity() != null)
			{
				synchronized (WeatherDataCollector.collected)
				{
					WeatherDataCollector.collected.add((WeatherReport) metadata);
				}
				debug("weather report saved for " + wr.getCity());
			}
		}
	}

}
