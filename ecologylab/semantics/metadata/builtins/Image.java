package ecologylab.semantics.metadata.builtins;

import java.io.File;
import java.util.Date;

import ecologylab.net.MimeType;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.DocumentLocationMap;
import ecologylab.semantics.html.documentstructure.ImageConstants;
import ecologylab.semantics.metadata.scalar.MetadataDate;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
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
	
//	/**
//	 * @return
//	 */
//	protected DocumentClosure constructClosure()
//	{
//		return new ImageClosure(this, semanticInlinks);
//	}

	/**
	 * Construct an ImageClipping object.
	 * @param source
	 * @param outlink
	 * @param caption TODO
	 * @param context TODO
	 * @return
	 */
	public ImageClipping constructClipping(Document source, Document outlink, String caption, String context)
	{
		ImageClipping result	= new ImageClipping(this, source, outlink, caption, context);
		this.addClipping(result);
		
		return result;
	}
	public ImageClipping constructClippingCandidate(Document source, Document outlink, String caption, String context)
	{
		return constructClippingCandidate(source, source, outlink, caption, context);
	}
	public ImageClipping constructClippingCandidate(Document basis, Document source, Document outlink, String caption, String context)
	{
		ImageClipping result	= constructClipping(source, outlink, caption, context);
		basis.addCandidateImage(this);
		return result;
	}
}
