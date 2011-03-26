/**
 * 
 */
package ecologylab.semantics.seeding;

import ecologylab.generic.Debug;
import ecologylab.oodss.messages.DefaultServicesTranslations;
import ecologylab.serialization.TranslationScope;

/**
 * TranslationSpace for client-side CFServices.
 * 
 * @author andruid
 */
public class BaseSeedTranslations extends Debug
{
	public static final String	TSCOPE_NAME	= "base_seed_translations";
	
	public static final Class	TRANSLATIONS[]	= 
	{ 
		Seed.class,
		SeedSet.class,
		
		DocumentState.class,
		SearchState.class,
		InlineSeed.class,
		Feed.class,
		Crawler.class,
		CuratedSeeding.class,
		DeliciousState.class,

		SeedCf.class,

	};

	/**
	 * 
	 */
	public BaseSeedTranslations()
	{
		super();

	}

	/**
	 * This accessor will work from anywhere, in any order, and stay efficient.
	 * @return	TranslationSpace for cF services.
	 */
	public static TranslationScope get()
	{
		return TranslationScope.get(TSCOPE_NAME, DefaultServicesTranslations.get(), TRANSLATIONS);
	}
}
