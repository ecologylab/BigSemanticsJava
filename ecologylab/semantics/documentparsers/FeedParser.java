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
import ecologylab.semantics.seeding.Seed;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 * 
 */
public class FeedParser
		extends LinksetParser
{
	
	private Feed feed;

	public FeedParser(InfoCollector infoCollector)
	{
		super(infoCollector);
		// TODO Auto-generated constructor stub
	}

	public FeedParser(InfoCollector infoCollector, Feed feed)
	{
		super(infoCollector);
		this.feed = feed;
		getMetaMetadataAndContainerAndQueue(infoCollector,feed.getUrl(),feed,"xml");
	}

	@Override
	public Document populateMetadata(SemanticActionHandler handler)
	{
		return directBindingPopulateMetadata();
	}

	@Override
	public Seed getSeed()
	{
		return feed;
	}
}
