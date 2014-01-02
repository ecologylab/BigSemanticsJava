package ecologylab.bigsemantics.collecting;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.scalar.MetadataParsedURL;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;

/**
 * @author andruid
 * 
 */
public class DocumentLocationMap<D extends Document>
{

  public static int           NUM_DOCUMENTS = 1024;

  DocumentMapHelper<D>        mapHelper;

  DocumentCache<ParsedURL, D> documentCache;

  public DocumentLocationMap(DocumentMapHelper<D> mapHelper,
                             DocumentCache<ParsedURL, D> documentCache)
  {
    this.mapHelper = mapHelper;
    this.documentCache = documentCache;
  }

  public Document get(ParsedURL location)
  {
    return documentCache.get(location);
  }

  /**
   * Look-up in map and get from there if possible. If not, construct by using location to lookup
   * meta-metadata. Then construct the subclass of Document that the meta-metadata specifies. Add it
   * to the map and return.
   * 
   * @param location
   * @param isImage
   * @return
   */
  public D getOrConstruct(ParsedURL location, boolean isImage)
  {
    D result = documentCache.get(location);
    if (result == null)
    {
      // record does not yet exist
      D newValue = mapHelper.constructValue(location, isImage);
      result = documentCache.putIfAbsent(newValue.getLocation(), newValue);
      if (result == null)
      {
        // put succeeded, use new value
        result = newValue;
      }
      else if (result != newValue)
      {
        result.addAdditionalLocation(newValue.getLocationMetadata());
        if (newValue.additionalLocations() != null)
          for (MetadataParsedURL newLoc : newValue.getAdditionalLocations())
            result.addAdditionalLocation(newLoc);
      }
    }
    return result;
  }

  public D getOrConstruct(MetaMetadata mmd, ParsedURL location)
  {
    D result = documentCache.get(location);
    if (result == null)
    {
      // record does not yet exist
      D newValue = mapHelper.constructValue(mmd, location);
      result = documentCache.putIfAbsent(location, newValue);
      if (result == null)
      {
        // put succeeded, use new value
        result = newValue;
      }
    }
    String inputName = mmd.getName();
    String resultName = result.getMetaMetadata().getName();
    if (!inputName.equals(resultName))
    {
      System.err.println("DocumentLocationMap.getOrConstruct() ERROR: Meta-metadata inputName="
                         + inputName
                         + " but resultName="
                         + resultName
                         + " for "
                         + location);
      result = null;
    }
    return result;
  }

  public void put(ParsedURL location, D document)
  {
    documentCache.put(location, document);
    ;
  }

  public void putIfAbsent(D document)
  {
    ParsedURL location = document.getLocation();
    if (location != null)
    {
      documentCache.putIfAbsent(location, document);
    }
  }

  /**
   * Add a new mapping, down the line, for an already mapped document, in the global map.
   * 
   * @param location
   * @param document
   */
  public void addMapping(ParsedURL location, Document document)
  {
    documentCache.put(location, (D) document);
  }

  /**
   * Change the mapped Document of reference for location to document. Also make sure that
   * newDocument's locations are mapped.
   * 
   * @param location
   *          The location that gets a new mapping.
   * @param newDocument
   *          The new document to be mapped to location.
   */
  public void remap(ParsedURL location, Document newDocument)
  {
    if (location != null)
    {
      documentCache.put(location, (D) newDocument);
      ParsedURL newDocumentLocation = newDocument.getLocation();
      if (newDocumentLocation != null && !location.equals(newDocumentLocation))
        documentCache.put(newDocumentLocation, (D) newDocument); // just to make sure
    }
    else
    {
      Debug.warning(this, "Location to remap is null! New doc: " + newDocument);
    }
  }

  /**
   * Change the mapped Document of reference for location to document.
   * 
   * @param oldDocument
   *          The document no longer of record, but whose location is being mapped.
   * @param newDocument
   *          The new document to be mapped to location.
   */
  public void remap(Document oldDocument, Document newDocument)
  {
    remap(oldDocument.getLocation(), newDocument);
  }

  public void remove(ParsedURL location)
  {
    documentCache.remove(location);
  }

  public void setRecycled(ParsedURL location)
  {
    documentCache.put(location, mapHelper.recycledValue());
  }

  public void setRecycled(MetadataParsedURL mPurl)
  {
    setRecycled(mPurl.getValue());
  }

  public void setUndefined(ParsedURL location)
  {
    documentCache.put(location, mapHelper.undefinedValue());
  }

  public boolean isRecycled(ParsedURL location)
  {
    return mapHelper.recycledValue() == documentCache.get(location);
  }

  public boolean isUndefined(ParsedURL location)
  {
    return mapHelper.undefinedValue() == documentCache.get(location);
  }

}
