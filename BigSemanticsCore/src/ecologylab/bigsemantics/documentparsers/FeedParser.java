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

  private Feed feed;

  public FeedParser(Feed feed)
  {
    super();
    this.feed = feed;
  }

  public void setSemanticScope(SemanticsGlobalScope semanticsScope)
  {
    super.setSemanticsScope(semanticsScope);
    getMetaMetadataAndContainerAndQueue(semanticsScope, feed.getUrl(), feed, "xml");
  }

  @Override
  public Document populateMetadata(Document document,
                                   MetaMetadataCompositeField metaMetadata,
                                   org.w3c.dom.Document DOM, SemanticActionHandler handler)
      throws IOException
  {
    return directBindingPopulateMetadata();
  }

  @Override
  public Seed getSeed()
  {
    return feed;
  }

}
