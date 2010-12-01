package ecologylab.semantics.metametadata.example;

import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.generated.library.WeatherReport;
import ecologylab.semantics.generated.library.scienceDirect.ScienceDirectArticle;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.generated.library.GoogleBook;
import ecologylab.semantics.generated.library.scholarlyPublication.ScholarlyArticle;

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
		
		if (metadata instanceof ScienceDirectArticle)
		{
			ScienceDirectArticle sda = (ScienceDirectArticle) metadata;
			if (sda.getTitle() != null)
			{
				synchronized (ScienceDirectDataCollector.collected)
				{
					ScienceDirectDataCollector.collected.add((ScienceDirectArticle) metadata);
				}
				debug("data saved for " + sda.getTitle());
			}
		}
		
		if (metadata instanceof GoogleBook)
		{
			GoogleBook gb = (GoogleBook) metadata;
			if (gb.getTitle() != null)
			{
				synchronized (GoogleBookDataCollector.collected)
				{
					GoogleBookDataCollector.collected.add((GoogleBook) metadata);
				}
				debug("data saved for " + gb.getTitle());
			}
		}
		
		if (metadata instanceof ScholarlyArticle)
		{
			ScholarlyArticle gb = (ScholarlyArticle) metadata;
			if (gb.getTitle() != null)
			{
//				synchronized (ACMDataCollector.collected)
//				{
//					ACMDataCollector.collected.add((ScholarlyArticle) metadata);
//				}
				debug("data saved for " + gb.getTitle());
			}
		}
	}
}

