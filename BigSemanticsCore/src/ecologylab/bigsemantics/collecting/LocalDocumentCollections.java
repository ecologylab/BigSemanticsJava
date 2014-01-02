package ecologylab.bigsemantics.collecting;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.Image;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;

/**
 * Singleton class, this master maps ParsedURLs to Document Metadata subclasses.
 * 
 * @author andruid
 */
public class LocalDocumentCollections extends Debug
{

  static public final Image             RECYCLED_IMAGE       = new Image(ParsedURL.getAbsolute("http://recycled.image"));

  static public final Image             UNDEFINED_IMAGE      = new Image(ParsedURL.getAbsolute("http://undefined.image"));

  static public final Image             UN_INFORMATIVE_IMAGE = new Image(ParsedURL.getAbsolute("http://uninformative.image"));

  private DocumentLocationMap<Document> allDocuments;

  LocalDocumentCollections(DocumentMapHelper<Document> documentMapHelper,
                           DocumentCache<ParsedURL, Document> documentCache)
  {
    super();
    allDocuments = new DocumentLocationMap<Document>(documentMapHelper, documentCache);
  }

  public Document lookupDocument(ParsedURL location)
  {
    return allDocuments.get(location);
  }

  /**
   * Basic operation on the DocumentLocationMap. Get from Map if possible. If necessary, construct
   * anew and add to map before returning.
   * 
   * @param location
   *          Location to get Document Metadata (subclass) for.
   * @param isImage
   *          If this location points to an image.
   * 
   * @return Associated Document Metadata (subclass).
   */
  public Document getOrConstruct(ParsedURL location, boolean isImage)
  {
    return allDocuments.getOrConstruct(location, isImage);
  }

  public void putIfAbsent(Document document)
  {
    allDocuments.putIfAbsent(document);
  }

  /**
   * Add a new mapping, down the line, for an already mapped document, in the global map.
   * 
   * @param location
   * @param document
   */
  public void addMapping(ParsedURL location, Document document)
  {
    allDocuments.addMapping(location, document);
  }

  /**
   * For registering an alternative Document subclass for a location. Typically performed because on
   * connect(), a more specific type (subclass) is selected from the MetaMetadataRepository.
   * 
   * @param oldDocument
   * @param newDocument
   */
  public void remap(Document oldDocument, Document newDocument)
  {
    allDocuments.remap(oldDocument, newDocument);
  }

  public void remove(ParsedURL location)
  {
    allDocuments.remove(location);
  }

  /**
   * Put a special entry into the DocumentLocationMap for the passed in location, saying that it
   * refers to an uninformative image that should be forever ignored.
   * 
   * @param location
   *          Location to ignore.
   */
  public void registerUninformativeImage(ParsedURL location)
  {
    allDocuments.put(location, UN_INFORMATIVE_IMAGE);
  }

  /**
   * Replace the entry (if there was one), or simply set, if this is first reference for this
   * location to the generic RECYCLED_DOCUMENT metadata.
   * 
   * @param location
   *          Location that is being recycled.
   */
  public void setRecycled(ParsedURL location)
  {
    allDocuments.setRecycled(location);
  }

//  public String debugString()
//  {
//    StringBuilder sb = new StringBuilder();
//    for (ParsedURL key : allDocuments.keySet())
//    {
//      sb.append(key).append("    ==>    ").append(allDocuments.get(key)).append("\n");
//    }
//    return sb.toString();
//  }

}
