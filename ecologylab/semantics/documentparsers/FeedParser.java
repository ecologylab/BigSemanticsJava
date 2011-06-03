/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.seeding.Feed;
import ecologylab.semantics.seeding.Seed;

/**
 * @author andruid
 * 
 */
public class FeedParser extends LinksetParser
{

	private Feed	feed;

	public FeedParser(NewInfoCollector infoCollector)
	{
		super(infoCollector);
		// TODO Auto-generated constructor stub
	}

	public FeedParser(NewInfoCollector infoCollector, Feed feed)
	{
		super(infoCollector);
		this.feed = feed;
		getMetaMetadataAndContainerAndQueue(infoCollector, feed.getUrl(), feed, "xml");
	}

	@Override
	public Document populateMetadata(CompoundDocument document,
			MetaMetadataCompositeField metaMetadata,
			org.w3c.dom.Document DOM, SemanticActionHandler handler)
	{
		return directBindingPopulateMetadata();
	}

	@Override
	public Seed getSeed()
	{
		return feed;
	}

}
