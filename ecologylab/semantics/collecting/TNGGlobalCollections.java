/**
 * 
 */
package ecologylab.semantics.collecting;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.TranslationScope;

/**
 * @author andruid
 *
 */
public class TNGGlobalCollections extends MetaMetadataRepositoryInit
{
	/**
	 */
	final DocumentLocationMap<Document>		allDocuments;
	final DocumentLocationMap<Image>			allImages;
	
	static public final Image 		RECYCLED_IMAGE		= new Image(ParsedURL.getAbsolute("http://recycled.image"));
	static public final Image 		UNDEFINED_IMAGE		= new Image(ParsedURL.getAbsolute("http://undefined.image"));
	static public final Image 		UN_INFORMATIVE_IMAGE = new Image(ParsedURL.getAbsolute("http://uninformative.image"));

	/**
	 * @param metaMetadataTranslations TODO
	 * 
	 */
	public TNGGlobalCollections(TranslationScope metaMetadataTranslations)
	{
		super(metaMetadataTranslations);
		
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
						return getMetaMetadataRepository().constructCompoundDocument(key);
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
		allImages	= new DocumentLocationMap<Image>(
				new DocumentMapHelper<Image>()
				{

					@Override
					public Image constructValue(ParsedURL key)
					{
						return getMetaMetadataRepository().constructImage(key);
					}

					@Override
					public Image recycledValue()
					{
						return RECYCLED_IMAGE;
					}

					@Override
					public Image undefinedValue()
					{
						return UNDEFINED_IMAGE;
					}

					@Override
					public Image constructValue(MetaMetadata mmd, ParsedURL key)
					{
						// TODO Auto-generated method stub
						return null;
					}
				});

	}

	public DocumentLocationMap<? extends Document> getGlobalDocumentMap()
	{
		return allDocuments;
	}
	public DocumentLocationMap<? extends Image> getGlobalImageMap()
	{
		return allImages;
	}
	
	public void registerUninformativeImage(ParsedURL key)
	{
		allImages.put(key, UN_INFORMATIVE_IMAGE);
	}
	
	public void recycleImage(Image toRecycle)
	{
		allImages.put(toRecycle.getLocation(), RECYCLED_IMAGE);
		toRecycle.recycle();
	}
}
