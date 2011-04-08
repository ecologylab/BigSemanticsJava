/**
 * 
 */
package ecologylab.semantics.connectors;

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
	final DocumentLocationMap<? extends Document>		allDocuments;
	final DocumentLocationMap<? extends Image>			allImages;
	
	static public final Image 		RECYCLED_IMAGE		= new Image(ParsedURL.getAbsolute("http://recycled.image"));
	static public final Image 		UNDEFINED_IMAGE		= new Image(ParsedURL.getAbsolute("http://undefined.image"));

	/**
	 * 
	 */
	public TNGGlobalCollections(final MetaMetadataRepository repository)
	{
		allDocuments	= new DocumentLocationMap<Document>(
				new DocumentMapHelper<Document>()
				{

					@Override
					public Document constructValue(ParsedURL key)
					{
						return repository.constructDocument(key);
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
						return repository.constructImage(key);
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
	public Document getOrConstructDocument(ParsedURL location)
	{
		return location == null ? null : allDocuments.getOrConstruct(location);
	}
	public Image getOrConstructImage(ParsedURL location)
	{
		return location == null ? null : allImages.getOrConstruct(location);
	}
}
