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

/**
 * @author andruid
 *
 */
public class TNGGlobalCollections extends Debug
{
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
					 * Construct a new Document, based on the ParsedURL.
					 * If there is no special meta-metadata for the location, construct a CompoundDocument.
					 * 
					 * @param key
					 * @return
					 */
					@Override
					public Document constructValue(ParsedURL key)
					{
						return repository.constructCompoundDocument(key);
					}

					@Override
					public Document constructValue(MetaMetadata mmd, ParsedURL key)
					{
						// TODO Auto-generated method stub
						return null;
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

	public DocumentLocationMap<? extends Document> getGlobalDocumentMap()
	{
		return allDocuments;
	}
	
	public void registerUninformativeImage(ParsedURL key)
	{
		allDocuments.put(key, UN_INFORMATIVE_IMAGE);
	}
	
	public void recycleDocument(Document toRecycle)
	{
		Document recycledThang = toRecycle.isImage() ? RECYCLED_IMAGE : Document.RECYCLED_DOCUMENT;
		allDocuments.put(toRecycle.getLocation(), recycledThang);
		toRecycle.recycle();
	}
	
	public Document getOrConstruct(ParsedURL location)
	{
		return allDocuments.getOrConstruct(location);
	}
	public void remap(Document oldDocument, Document newDocument)
	{
		allDocuments.remap(oldDocument, newDocument);
	}
	
	public void setRecycled(ParsedURL location)
	{
		allDocuments.setRecycled(location);
	}
}
