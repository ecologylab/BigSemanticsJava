package ecologylab.semantics.downloaders.oodss;

import ecologylab.oodss.messages.DefaultServicesTranslations;
import ecologylab.serialization.SimplTypesScope;

public class SemanticServiceOODSSTranslationScope {

	private static SimplTypesScope oodssTranslationScope = null;
	
	public static SimplTypesScope getOODSSTranslationScope()
	{
		if (oodssTranslationScope == null)
		{
			/*
			 *  get base translations with static accessor
			 */
			SimplTypesScope baseServices = DefaultServicesTranslations.get();
			
			//SimplTypesScope builtins = MetadataBuiltinsTypesScope.get();
			
			/* 
			 * Classes that must be translated by the translation scope
			 * in order for the server to communicate w/ the client
			 */
			Class[] lookupMetadataClasses = { DownloadRequest.class,
					 DownloadResponse.class};
			
			/*
			 * compose translations, to create the 
			 * space inheriting the base translations
			 */
			oodssTranslationScope = SimplTypesScope.get("lookupMetadata",
					baseServices, lookupMetadataClasses);
		}
		return oodssTranslationScope;
	}
}
