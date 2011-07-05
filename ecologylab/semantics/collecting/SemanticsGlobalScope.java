/**
 * 
 */
package ecologylab.semantics.collecting;

import ecologylab.collections.Scope;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.TranslationScope;

/**
 * 
 * The SemanticsScope (also known as Crossroads) contains references to 
 * all of the big global singleton object of S.IM.PL Semantics:
 * (1) GlobalCollection that maps ParsedURL keys to <? extends Document> values.
 * (2) MetaMetadataRespository
 * (3) SemanticsDownloadMonitors -- a set of DownloadMonitors, with different priority levels and media assignments.
 * 
 * The SemanticsSessionScope will include references to Crossroads and to Crawler, if there is one. 
 * I believe it is also where we store state related to Seeding. 
 * 
 * @author andruid
 */
public class SemanticsGlobalScope extends MetaMetadataRepositoryInit
{
	/**
	 * Maps locations to Document Metadata subclasses. 
	 * Constructs these Document instances as needed using the MetaMetadataRepository.
	 */
	final protected TNGGlobalCollections				globalCollection;
	
	/**
	 * Pool of DownloadMonitors used for parsing Documents of various types.
	 */
	final private		SemanticsDownloadMonitors		downloadMonitors;
	
	public SemanticsGlobalScope(TranslationScope metadataTScope)
	{
		super(metadataTScope);
		
		globalCollection	= TNGGlobalCollections.getSingleton(getMetaMetadataRepository());
		
		downloadMonitors	= new SemanticsDownloadMonitors();
	}

	/**
	 * @return the globalCollection
	 */
	public TNGGlobalCollections getGlobalCollection()
	{
		return globalCollection;
	}

	/**
	 * Pool of DownloadMonitors used for parsing Documents of various types.
	 * 
	 * @return the downloadMonitors
	 */
	public SemanticsDownloadMonitors getDownloadMonitors()
	{
		return downloadMonitors;
	}	
}
