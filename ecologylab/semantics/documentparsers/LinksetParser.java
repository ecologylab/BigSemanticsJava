/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.generic.DispatchTarget;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.connectors.old.OldContainerI;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;

/**
 * This is the base class for all seeds which bascially return a collection of links to be
 * interleaved with other seeds. Example of such seeds are search result pages and feeds.
 * 
 * @author andruid
 * 
 */
public abstract class LinksetParser
		extends ParserBase implements DispatchTarget<OldContainerI>
{

	public LinksetParser(NewInfoCollector infoCollector)
	{
		super(infoCollector);
	}

	/**
	 * @param infoCollector
	 * @param seed
	 * @param defaultTag TODO
	 */
	protected void getMetaMetadataAndContainerAndQueue(NewInfoCollector infoCollector, ParsedURL purl, Seed seed, String defaultTag)
	{
		OldContainerI container = infoCollector.getContainer(null, null, metaMetadata, purl, false, false, false);
		setContainer(container);
		container.presetDocumentType(this);
		container.setDispatchTarget(this);
		container.setAsTrueSeed(seed);

		seed.queueSeedOrRegularContainer(container);
	}


	/**
	 * call doneQueueing() to notify seed distributor
	 */
	public void delivery(OldContainerI downloadedContainer)
	{
		Seed seed = downloadedContainer.getSeed();
		if (seed != null)
		{
			SeedDistributor aggregator = seed.seedDistributer(infoCollector);
			if (aggregator != null)
				aggregator.doneQueueing(downloadedContainer);
		}
	}
	
	/**
	 * Connects to a SeedDistributor, when appropriate.
	 * 
	 * @return the Seed that initiated this, if it is seeding time, or null, if it is not.
	 */
	@Override
	public Seed getSeed()
	{
		return searchSeed;
	}

	public void incrementResultSoFar()
	{
		this.resultsSoFar++;
	}

	public int getResultSoFar()
	{
		return this.resultsSoFar;
	}

	public int getResultNum()
	{
		return resultsSoFar + searchSeed.currentFirstResultIndex();
	}

	

}
