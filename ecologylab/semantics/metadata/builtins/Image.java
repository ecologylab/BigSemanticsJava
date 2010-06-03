package ecologylab.semantics.metadata.builtins;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.xml_inherit;

/**
 * This is not generated code, but a hand-authored base class in the 
 * Metadata hierarchy. It is hand-authored in order to provide specific functionalities
 **/
@xml_inherit
public class Image extends Media
{
	@xml_leaf
	MetadataString					caption;

	@xml_leaf
	MetadataParsedURL				location;

	@xml_leaf
	private MetadataString	localLocation;

	public Image()
	{

	}

	public Image(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
	}

	MetadataString caption()
	{
		MetadataString result = this.caption;
		if (result == null)
		{
			result = new MetadataString();
			this.caption = result;
		}
		return result;
	}

	MetadataParsedURL location()
	{
		MetadataParsedURL result = this.location;
		if (result == null)
		{
			result = new MetadataParsedURL();
			this.location = result;
		}
		return result;
	}

	public String getCaption()
	{
		// return caption;
		return caption().getValue();
	}

	@Override
	public ParsedURL getLocation()
	{
		// return location;
		return location().getValue();
	}

	public void hwSetCaption(String caption)
	{
		// this.caption = caption;
		this.setCaption(caption);
		rebuildCompositeTermVector();
	}

	@Override
	public void hwSetLocation(ParsedURL location)
	{
		// this.location = location;
		this.setLocation(location);
		rebuildCompositeTermVector();
	}

	public void setCaption(String caption)
	{
		// this.caption = caption;
		this.caption().setValue(caption);
	}

	@Override
	public void setLocation(ParsedURL location)
	{
		// this.location = location;
		this.location().setValue(location);
	}


	@Override
	public void hwSetNavLocation(ParsedURL navLocation)
	{
		// this.navLocation = navLocation;
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
}
