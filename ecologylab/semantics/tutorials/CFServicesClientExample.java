package ecologylab.semantics.tutorials;


import ecologylab.generic.Debug;
import ecologylab.generic.Generic;
import ecologylab.oodss.distributed.common.ServicesHostsAndPorts;
import ecologylab.oodss.distributed.legacy.ServicesClient;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.semantics.seeding.BaseSeedTranslations;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.SeedCf;
import ecologylab.serialization.TranslationScope;

/**
 * Example of how to use ecologylab.oodss to form a combinFormation client,
 * and send search queries and such to the server.
 * 
 * @author andruid
 */
public class CFServicesClientExample extends Debug
{
	static final TranslationScope	CF_TRANSLATIONS		= BaseSeedTranslations.get();
	 
	/**
	 * 
	 */
	public CFServicesClientExample()
	{
		super();
	}

	static final String[] G_QUERIES =
	{
		"ann hamilton installation",
		"information visualization interfaces",
		"creativity and education",
		"aggie basketball",
	};

	static final String[] N_QUERIES =
	{
		"tv"
	};
	
	public static void main(String[] args)
	{
		ServicesClient cfClient	= new ServicesClient(ServicesHostsAndPorts.CF_SERVICES_PORT, CF_TRANSLATIONS);
		
		// in case the server is not up yet, wait for it.
		cfClient.waitForConnect();

//		sendQueriesToCF(cfClient, "google", G_QUERIES);
		sendQueriesToCF(cfClient, "yahoo_buzz", N_QUERIES);
	}


/**
 * Send a bunch of queries to the search engine specified.
 * Wait 25 seconds after each query.
 * 
 * @param cfClient
 * @param engine
 * @param queries
 */
	private static void sendQueriesToCF(ServicesClient cfClient, String engine, String[] queries)
	{
		SeedCf	seedCf		= new SeedCf();
		
		seedCf.setHandleMultipleRequests(SeedCf.MULTIPLE_REQUESTSTS_ASK_USER);
		
		for (int i=0; i<queries.length; i++)
		{
			String query = queries[i];
			println("Sending to cF, engine " + engine + " query: " + query);
			SearchState	searchState	= new SearchState(engine, query);
		
			seedCf.add(searchState);
		
			// send message and wait for response
			ResponseMessage response	= cfClient.sendMessage(seedCf);
		
			if (!response.isOK())
			{
				println("Bad response from server. Quitting.\n" + response);
				return;
			}
			
			seedCf.clear();
			println("Wait for 25 seconds");
			// wait for 25 seconds
			Generic.sleep(25000);
		}
	}

}
