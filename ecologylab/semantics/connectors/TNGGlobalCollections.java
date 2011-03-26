/**
 * 
 */
package ecologylab.semantics.connectors;

import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.metametadata.MetaMetadataRepository;

/**
 * @author andruid
 *
 */
public class TNGGlobalCollections extends Debug
{
	final DocumentLocationMap<Document>		allDocuments;
	final DocumentLocationMap<Image>			allImages;
	
	static public final Document 	RECYCLED_DOCUMENT	= new Document(ParsedURL.getAbsolute("http://recycled.document"));
	static public final Document 	UNDEFINED_DOCUMENT= new Document(ParsedURL.getAbsolute("http://undefined.document"));

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
					public Document recycledValue()
					{
						return RECYCLED_DOCUMENT;
					}

					@Override
					public Document undefinedValue()
					{
						return UNDEFINED_DOCUMENT;
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
				});

	}

}
