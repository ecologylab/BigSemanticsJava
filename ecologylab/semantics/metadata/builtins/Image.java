package ecologylab.semantics.metadata.builtins;

import java.io.File;
import java.util.HashMap;

import ecologylab.net.MimeType;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.documentstructure.ImageConstants;
import ecologylab.semantics.metadata.scalar.MetadataFile;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.simpl_inherit;

/**
 * This is not generated code, but a hand-authored base class in the 
 * Metadata hierarchy. It is hand-authored in order to provide specific functionalities
 **/
@simpl_inherit
public class Image extends Document
implements MimeType, ImageConstants
{
	@mm_name("local_location") 
	@simpl_scalar
	private MetadataParsedURL	localLocation;
	
	static final HashMap<String, Integer>		mimeTypeToIndexMap		= new HashMap<String, Integer>(15);

	static
	{
		mimeTypeToIndexMap.put("image/jpeg", JPG);
		mimeTypeToIndexMap.put("image/pjpeg", JPG);
		mimeTypeToIndexMap.put("image/gif", GIF);
		mimeTypeToIndexMap.put("image/png", PNG);
	}

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
	
	@Override
	public boolean isImage()
	{
		return true;
	}
	

	/**
	 * Get index indicating mimeType. May be used for designRole() and in weighting.
	 * 
	 * @param parsedURL
	 */
	public static int mimeIndexFromMimeType ( String mimeType )
	{
		return Image.mimeTypeToIndexMap.get(mimeType);
//		return (mimeSlot != null) ? mimeSlot.ge : UNKNOWN_MIME;
	}
}
