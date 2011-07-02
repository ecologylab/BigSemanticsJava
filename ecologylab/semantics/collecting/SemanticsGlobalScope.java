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
 * A subclass, InteractiveSemanticsSessionScope, will also include a reference to the AWTBridge. 
 * A likely further subclass is CfSemanticsSessionScope.
 * 
 * @author andruid
 */
public class SemanticsGlobalScope extends MetaMetadataRepositoryInit
{
	final protected TNGGlobalCollections				globalCollection;
	
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
	 * @return the downloadMonitors
	 */
	public SemanticsDownloadMonitors getDownloadMonitors()
	{
		return downloadMonitors;
	}	
}
