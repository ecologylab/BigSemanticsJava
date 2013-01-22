/**
 * 
 */
package ecologylab.semantics.documentparsers;

import java.io.IOException;

import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
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