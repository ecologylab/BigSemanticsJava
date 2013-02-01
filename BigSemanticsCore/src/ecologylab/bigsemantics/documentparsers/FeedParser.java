/**
 * 
 */
package ecologylab.bigsemantics.documentparsers;

import java.io.IOException;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.seeding.Feed;
import ecologylab.bigsemantics.seeding.Seed;

/**
 * @author andruid
 * 
 */
public class FeedParser extends LinksetParser
{

	private Feed	feed;

	public FeedParser(SemanticsGlobalScope infoCollector)
	{
		super(infoCollector);
		// TODO Auto-generated constructor stub
	}

	public FeedParser(SemanticsGlobalScope infoCollector, Feed feed)
	{
		super(infoCollector);
		this.feed = feed;
		getMetaMetadataAndContainerAndQueue(infoCollector, feed.getUrl(), feed, "xml");
	}

	@Override
	public Document populateMetadata(Document document,
			MetaMetadataCompositeField metaMetadata,
			org.w3c.dom.Document DOM, SemanticActionHandler handler) throws IOException
	{
		return directBindingPopulateMetadata();
	}

	@Override
	public Seed getSeed()
	{
		return feed;
	}

}
