/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.seeding.Feed;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 * 
 */
public class MetaMetadataFeedParser
		extends MetaMetadataLinksetParser
{

	public MetaMetadataFeedParser(InfoCollector infoCollector)
	{
		super(infoCollector);
		// TODO Auto-generated constructor stub
	}

	public MetaMetadataFeedParser(InfoCollector infoCollector, SemanticActionHandler semanticActionHandler,
			Feed feed)
	{
		super(infoCollector, semanticActionHandler);
		getMetaMetadataAndContainerAndQueue(infoCollector,feed.getUrl(),feed,null);
	}

	@Override
	public Document populateMetadata()
	{
		return directBindingPopulateMetadata();
	}
	
	

}
