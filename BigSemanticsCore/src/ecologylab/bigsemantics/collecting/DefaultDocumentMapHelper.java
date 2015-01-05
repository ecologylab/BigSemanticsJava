package ecologylab.bigsemantics.collecting;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.net.ParsedURL;

/**
 * The default DocumentMapHelper implementation. This helper creates Document objects using its
 * corresponding MetaMetadata when available, or the selector mechanism to find a MetaMetadata for
 * the given location.
 * 
 * @author quyin
 */
public class DefaultDocumentMapHelper implements DocumentMapHelper<Document>
{

  private MetaMetadataRepository repository;

  public DefaultDocumentMapHelper(MetaMetadataRepository repository)
  {
    this.repository = repository;
  }

  /**
   * Construct a new Document, based on the ParsedURL, and lookup in the MetaMetadataRepository. If
   * there is no special meta-metadata for the location, construct a CompoundDocument. Either way,
   * set its location.
   * 
   * @param location
   *          Location of the Document to construct. Fed to MetaMetadata selectors maps.
   * 
   * @return Newly constructed Document (subclass), based on the location.
   */
  @Override
  public Document constructValue(ParsedURL location, boolean isImage)
  {
    Document result = null;
    try
    {
      result = repository.constructDocument(location, isImage);
    }
    catch (MetaMetadataException e)
    {
      e.printStackTrace();
    }
    return result;
  }

  /**
   * Construct a new Document, using the supplied MetaMetadata. Set its location.
   * 
   */
  @Override
  public Document constructValue(MetaMetadata mmd, ParsedURL location)
  {
    Document document = (Document) mmd.constructMetadata();
    document.setLocation(location);
    return document;
  }

  @Override
  public Document recycledValue()
  {
    return Document.RECYCLED_DOCUMENT;
  }

  @Override
  public Document undefinedValue()
  {
    return Document.UNDEFINED_DOCUMENT;
  }

}
