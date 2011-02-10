/**
 * 
 */
package ecologylab.semantics.seeding;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.documentparsers.FeedParser;

/**
 * A Feed is a Document Seed whose links are interleaved by a SeedDistributor, enabling round-robin scheduling
 * of downloads.
 * 
 * @author andruid
 * @author abhinav
 */
public class Feed extends DocumentState
{

	private String queryString;
	
	/**
	 * 
	 */
	public Feed()
	{

	}

	/**
	 * @param purlString
	 * @param action
	 */
	public Feed(String purlString, String action)
	{
		super(purlString, action);
	}

	/**
	 * @param purl
	 */
	public Feed(ParsedURL purl)
	{
		super(purl);
		// TODO Auto-generated constructor stub
	}

	

  protected boolean useDistributor()
  {
  	return true;
  }
  
  @Override
	public void performInternalSeedingSteps(InfoCollector infoCollector)
	{
	 new FeedParser(infoCollector, this);
	}
  
	/**
	 * Called after a seed is parsed to prevent it being parsed again later during re-seeding.
	 * This override does nothing, because Feed seeds should remain active.
	 * 
	 * @param inActive the inActive to set
	 */
	@Override
	public void setActive(boolean value)
	{
		
	}

	/**
	 * (current for debug only)
	 */
	@Override
	public String getQuery()
	{
		if (queryString == null)
		{
			queryString = "Feed(" + url + ")";
		}
		return queryString;
	}
	
}
