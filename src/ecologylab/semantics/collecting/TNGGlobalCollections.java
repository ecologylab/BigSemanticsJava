/**
 * 
 */
package ecologylab.semantics.collecting;

import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.exceptions.MetaMetadataException;

/**
 * Singleton class, this master maps ParsedURLs to Document Metadata subclasses.
 * 
 * @author andruid
 */
public class TNGGlobalCollections extends Debug
{
	/**
	 * Singleton instance because we only allow construction of one of this.
	 */
	private static TNGGlobalCollections singleton;
	
	public static final synchronized TNGGlobalCollections getSingleton(final MetaMetadataRepository repository)
	{
		TNGGlobalCollections result	= singleton;
		if (result == null)
		{
			result		= new TNGGlobalCollections(repository);
			singleton	= result;
		}
		return result;
	}
	/**
	 */
	final private DocumentLocationMap<Document>		allDocuments;
	
	static public final Image 		RECYCLED_IMAGE				= new Image(ParsedURL.getAbsolute("http://recycled.image"));
	static public final Image 		UNDEFINED_IMAGE				= new Image(ParsedURL.getAbsolute("http://undefined.image"));
	static public final Image 		UN_INFORMATIVE_IMAGE	= new Image(ParsedURL.getAbsolute("http://uninformative.image"));

	/**
	 * @param repository 	
	 */
	private TNGGlobalCollections(final MetaMetadataRepository repository)
	{
		super();
		
		allDocuments	= new DocumentLocationMap<Document>(
				new DocumentMapHelper<Document>()
				{

					/**
					 * Construct a new Document, based on the ParsedURL, and lookup in the MetaMetadataRepository.
					 * If there is no special meta-metadata for the location, construct a CompoundDocument.
					 * Either way, set its location.
					 * 
					 * @param location	Location of the Document to construct. Fed to MetaMetadata selectors maps.
					 * 
					 * @return					Newly constructed Document (subclass), based on the location.
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
					 * Construct a new Document, using the supplied MetaMetadata.
					 * Set its location.
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
				});
	}
	
	public Document lookupDocument(ParsedURL location)
	{
		return allDocuments.get(location);
	}
	
	/**
	 * Basic operation on the DocumentLocationMap.
	 * Get from Map if possible. If necessary, construct anew and add to map before returning.
	 * 
	 * @param location	Location to get Document Metadata (subclass) for.
	 * @param isImage TODO
	 * 
	 * @return					Associated Document Metadata (subclass).
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
	 * Put a special entry into the DocumentLocationMap for the passed in location, 
	 * saying that it refers to an uninformative image that should be forever ignored.
	 * 
	 * @param location	Location to ignore.
	 */
	public void registerUninformativeImage(ParsedURL location)
	{
		allDocuments.put(location, UN_INFORMATIVE_IMAGE);
	}
	
/**
 * Replace the entry (if there was one), or simply set, if this is first reference
 * for this location to the generic RECYCLED_DOCUMENT metadata.
 * 
 * @param location	Location that is being recycled.
 */
	public void setRecycled(ParsedURL location)
	{
		allDocuments.setRecycled(location);
	}

	/**
	 * For registering an alternative Document subclass for a location.
	 * Typically performed because on connect(), a more specific type (subclass) is selected from the MetaMetadataRepository.
	 * 
	 * @param oldDocument
	 * @param newDocument
	 */
	public void remap(Document oldDocument, Document newDocument)
	{
		allDocuments.remap(oldDocument, newDocument);
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
	
	public String debugString()
	{
	  StringBuilder sb = new StringBuilder();
	  for (ParsedURL key : allDocuments.keySet())
	  {
      sb.append(key).append("    ==>    ").append(allDocuments.get(key)).append("\n");
	  }
	  return sb.toString();
	}
	
}
