/**
 * 
 */
package ecologylab.bigsemantics.documentparsers;

import java.io.IOException;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.actions.SemanticActionsKeyWords;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.namesandnums.CFPrefNames;
import ecologylab.bigsemantics.namesandnums.DocumentParserTagNames;
import ecologylab.bigsemantics.seeding.SearchState;
import ecologylab.net.ParsedURL;

/**
 * @author amathur
 * 
 */
public class SearchParser extends LinksetParser implements CFPrefNames, SemanticActionsKeyWords
{

  /**
   * The Search query URL
   */
  private ParsedURL searchURL;

  private int       resultsSoFar = 0;

  public SearchParser(SearchState searchSeed)
  {
    super();
    this.searchSeed = searchSeed;
    this.searchURL = searchSeed.formSearchUrlBasedOnEngine();

  }

  public void setSemanticsScope(SemanticsGlobalScope semanticsScope)
  {
    // if search PURL is not null for a container.
    if (searchURL != null)
    {
      searchSeed.eliminatePlusesFromQuery();
      getMetaMetadataAndContainerAndQueue(semanticsScope,
                                          searchURL,
                                          searchSeed,
                                          DocumentParserTagNames.SEARCH_TAG);
    }
  }

  public ParsedURL purl()
  {
    return searchURL;
  }

  @Override
  public Document populateMetadata(Document document,
                                   MetaMetadataCompositeField metaMetadata,
                                   org.w3c.dom.Document DOM,
                                   SemanticActionHandler handler) throws IOException
  {
    Document resultingDocument = document;

    MetaMetadata mmd = null;
    // FIXME with the new inheritance and inilne MMD processing, this should always be true
    if (metaMetadata instanceof MetaMetadata)
    {
      mmd = (MetaMetadata) metaMetadata;
    }
    else
    {
      warning("Not a MetaMetadata: "
              + metaMetadata
              + ": something wrong with the inheritance and inline MMD processing!");
    }

    // FIXME use overrides instead of constants here!!!!!!!!!!
    if (DIRECT_BINDING_PARSER.equals(mmd.getParser()))
    {
      resultingDocument = directBindingPopulateMetadata();

      // FIXME -- copy values like query from original metadata to the new one!!!
    }
    else if (XPATH_PARSER.equals(mmd.getParser()))
    {
      recursiveExtraction(metaMetadata,
                          resultingDocument,
                          DOM,
                          null,
                          handler.getSemanticActionVariableMap());
      // container.setMetadata(populatedMetadata);
    }

    return resultingDocument;
  }

  @Override
  public void callback(DocumentClosure downloadedContainer)
  {
    super.callback(downloadedContainer);
    if (searchSeed != null)
      searchSeed.incrementNumResultsBy(searchSeed.numResults());
  }

}