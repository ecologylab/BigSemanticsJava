package ecologylab.semantics.metadata.builtins;

import java.io.File;
import java.util.Date;

import ecologylab.net.MimeType;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.html.documentstructure.ImageConstants;
import ecologylab.semantics.metadata.builtins.declarations.ImageDeclaration;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * This is not generated code, but a hand-authored base class in the 
 * Metadata hierarchy. It is hand-authored in order to provide specific functionalities
 **/
@simpl_inherit
public class Image extends ImageDeclaration
implements MimeType, ImageConstants
{
	
//	@mm_name("local_location") 
//	@simpl_scalar
//	private MetadataParsedURL	localLocation;
//	
//	@mm_name("creation_date") 
//	@simpl_scalar
//	private MetadataDate	creationDate;
	
	/**
	 * Number of images that we parsed with an alt-text.
	 */
	public static int		hasAlt;

	public Image()
	{
		super();
	}

	public Image(MetaMetadataCompositeField metaMetadata)
	{
		super(metaMetadata);
	}

	/**
	 * Construct an instance of this, the base document type, and set its location.
	 * 
	 * @param location
	 */
	public Image(ParsedURL location)
	{
		this(MetaMetadataRepository.getBaseDocumentMM());
		setLocation(location);
	}

	@Override
	public void hwSetNavLocation(ParsedURL navLocation)
	{
		this.setNavLocation(navLocation);
		rebuildCompositeTermVector();
	}
	
	public File getLocalLocationAsFile()
	{
		ParsedURL localLocationPurl = getLocalLocation();
		return (localLocationPurl != null) ? localLocationPurl.file() : null;
	}
	
	/**
	 * Does not actually rebuildCompositeTermVector() as date should never contribute to it.
	 * @param date
	 */
	public void hwSetCreationDate(Date date)
	{
		setCreationDate(date);
	}

	/**
	 * Convenience method for type checking related to Image-ness.
	 * Implementation for Image and its subclasses:
	 * 
	 * @return	true
	 */
	@Override
	public boolean isImage()
	{
		return true;
	}
	
	/**
	 * Use the local location if there is one; otherwise, just use the regular location.
	 * 
	 * @return
	 */
	@Override
	public ParsedURL getDownloadLocation()
	{
		ParsedURL result	= getLocation();
		ParsedURL localLocation	= getLocalLocation();
		if (localLocation != null)
		{
			File localFile	= localLocation.file();
			if (localFile.exists())
				result	= localLocation;
		}
		return result;
	}

	/**
	 * Construct an ImageClipping object.
	 * 
	 * @param sourceDocument	The CompoundDocument to be listed as the Clipping's source. The one it is a surrogate for.
	 * 												Usually the same as basisDocument, but for a surrogate for X, found in Y, instead uses outlink here.
	 * @param outlink					The Document to be listed as the Clipping's href destination.
	 * @param caption					Caption text extracted from basisDocument.
	 * @param context					Larger paragraph of contextualizing text extracted from basisDocument.
	 * 
	 * @return								New ImageClipping.
	 */
	public ImageClipping constructClipping(Document sourceDocument, Document outlink, String caption, String context)
	{
		ImageClipping result	= new ImageClipping(SemanticsSessionScope.get().IMAGE_CLIPPING_META_METADATA, this, sourceDocument, outlink, caption, context);
		this.addClipping(result);
		
		return result;
	}

	/**
	 * Construct an ImageClipping object. Add it to the basis's Collection<Clipping>.
	 * 
	 * @param basisDocument		The CompoundDocument to add the clipping to. 
	 * @param sourceDocument	The CompoundDocument to be listed as the Clipping's source. The one it is a surrogate for.
	 * 												Usually the same as basisDocument, but for a surrogate for X, found in Y, instead uses outlink here.
	 * @param outlink					The Document to be listed as the Clipping's href destination.
	 * @param caption					Caption text extracted from basisDocument.
	 * @param context					Larger paragraph of contextualizing text extracted from basisDocument.
	 * 
	 * @return								New ImageClipping.
	 */
	public ImageClipping constructClipping(Document basisDocument, Document sourceDocument, Document outlink, String caption, String context)
	{
		ImageClipping result	= constructClipping(sourceDocument, outlink, caption, context);
		basisDocument.addClipping(result);
		return result;
	}
	
	
}
