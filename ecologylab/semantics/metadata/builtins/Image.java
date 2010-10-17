package ecologylab.semantics.metadata.builtins;

import java.util.HashMap;

import ecologylab.collections.CollectionTools;
import ecologylab.generic.IntSlot;
import ecologylab.net.MimeType;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.documentstructure.ImageConstants;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.simpl_inherit;

/**
 * This is not generated code, but a hand-authored base class in the 
 * Metadata hierarchy. It is hand-authored in order to provide specific functionalities
 **/
@simpl_inherit
public class Image extends ClippableDocument
implements MimeType, ImageConstants
{
	@mm_name("local_location") 
	@simpl_scalar
	private MetadataString	localLocation;
	
	static final HashMap<String, Integer>		mimeTypeToIndexMap		= new HashMap<String, Integer>(15);

	static
	{
		mimeTypeToIndexMap.put("image/jpeg", JPG);
		mimeTypeToIndexMap.put("image/pjpeg", JPG);
		mimeTypeToIndexMap.put("image/gif", GIF);
		mimeTypeToIndexMap.put("image/png", PNG);
	}



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

	public MetadataString localLocation()
	{
		MetadataString result = this.localLocation;
		if (result == null)
		{
			result = new MetadataString();
			this.localLocation = result;
		}
		return result;
	}

	/**
	 * Gets the value of the field localLocation
	 **/

	public String getLocalLocation()
	{
		return localLocation().getValue();
	}

	/**
	 * Sets the value of the field localLocation
	 **/

	public void setLocalLocation(String localLocation)
	{
		this.localLocation().setValue(localLocation);
	}

	/**
	 * The heavy weight setter method for field localLocation
	 **/

	public void hwSetLocalLocation(String localLocation)
	{
		this.localLocation().setValue(localLocation);
		rebuildCompositeTermVector();
	}

	/**
	 * Sets the localLocation directly
	 **/

	public void setLocalLocationMetadata(MetadataString localLocation)
	{
		this.localLocation = localLocation;
	}

	/**
	 * Heavy Weight Direct setter method for localLocation
	 **/

	public void hwSetLocalLocationMetadata(MetadataString localLocation)
	{
		if (this.localLocation != null && this.localLocation.getValue() != null && hasTermVector())
			termVector().remove(this.localLocation.termVector());
		this.localLocation = localLocation;
		rebuildCompositeTermVector();
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
