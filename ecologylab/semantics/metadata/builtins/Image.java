package ecologylab.semantics.metadata.builtins;

import java.io.File;
import java.util.Date;

import ecologylab.net.MimeType;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.html.documentstructure.ImageConstants;
import ecologylab.semantics.metadata.scalar.MetadataDate;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.simpl_inherit;

/**
 * This is not generated code, but a hand-authored base class in the 
 * Metadata hierarchy. It is hand-authored in order to provide specific functionalities
 **/
@simpl_inherit
public class Image extends ClippableDocument<Image>
implements MimeType, ImageConstants
{
	@mm_name("local_location") 
	@simpl_scalar
	private MetadataParsedURL	localLocation;
	
	@mm_name("creation_date") 
	@simpl_scalar
	private MetadataDate	creationDate;
	
	/**
	 * Number of images that we parsed with an alt-text.
	 */
	public static int		hasAlt;

	public Image()
	{

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
		super(location);
	}

	@Override
	public void hwSetNavLocation(ParsedURL navLocation)
	{
		this.setNavLocation(navLocation);
		rebuildCompositeTermVector();
	}
	

	/**
	 * Lazy Evaluation for localLocation
	 **/

	public MetadataParsedURL localLocation()
	{
		return this.localLocation;
	}
	
	public ParsedURL getLocalLocationPurl()
	{
		return (localLocation != null) ? localLocation.getValue() : null;
	}
	
	public MetadataParsedURL getLocalLocationMetadata()
	{
		return localLocation;
	}
	
	public File getLocalLocationAsFile()
	{
		ParsedURL localLocationPurl = getLocalLocationPurl();
		return (localLocationPurl != null) ? localLocationPurl.file() : null;
	}

	/**
	 * Sets the localLocation directly
	 **/

	public void setLocalLocation(ParsedURL localLocationFile)
	{
		if (this.localLocation == null)
			this.localLocation = new MetadataParsedURL(localLocationFile);
		else
			this.localLocation.setValue(localLocationFile);
	}
	
	public void setLocalLocationMetadata(MetadataParsedURL localLocation)
	{
		this.localLocation = localLocation;
	}

	/**
	 * Lazy Evaluation for creationDate
	 **/

	public MetadataDate creationDate()
	{
		MetadataDate result = this.creationDate;
		if (result == null)
		{
			result = new MetadataDate();
			this.creationDate = result;
		}
		return result;
	}

	public Date getCreationDate()
	{
		return creationDate == null ? null : creationDate.getValue();
	}

	public MetadataDate getCreationDateMetadata()
	{
		return creationDate;
	}
	
	public void setCreationDateMetadata(MetadataDate creationDate)
	{
		this.creationDate = creationDate;
	}

	/**
	 * Sets the creationDate directly
	 **/

	public void setCreationDate(Date date)
	{
		if (date != null)
			creationDate().setValue(date);
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
		if (localLocation != null)
		{
			ParsedURL locationLocationPurl	= getLocalLocationPurl();
			File localFile	= locationLocationPurl.file();
			if (localFile.exists())
				result	= locationLocationPurl;
		}
		return result;
	}

//	/**
//	 * @return
//	 */
//	protected DocumentClosure constructClosure()
//	{
//		return new ImageClosure(this, semanticInlinks);
//	}

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
		ImageClipping result	= new ImageClipping(SemanticsSessionScope.IMAGE_CLIPPING_META_METADATA, this, sourceDocument, outlink, caption, context);
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
