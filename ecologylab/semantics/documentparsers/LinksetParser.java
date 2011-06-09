/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.generic.Continuation;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.seeding.DocumentState;
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
		extends ParserBase implements Continuation<DocumentClosure>
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
		Document document								= infoCollector.getGlobalDocumentMap().getOrConstruct(purl);
		DocumentClosure documentClosure	= document.getOrConstructClosure();
		if (documentClosure != null)
		{
//			container.presetDocumentType(this);
			documentClosure.setDocumentParser(this);
			documentClosure.setContinuation(this);
			document.setAsTrueSeed(seed);

			seed.queueSeedOrRegularContainer(documentClosure);			
		}
	}


	/**
	 * call doneQueueing() to notify seed distributor
	 */
	public void callback(DocumentClosure sourceClosure)
	{
		Seed seed = sourceClosure.getSeed();
		if (seed != null)
		{
			SeedDistributor aggregator = seed.seedDistributer(infoCollector);
			if (aggregator != null)
				aggregator.doneQueueing(sourceClosure.getDocument());
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
