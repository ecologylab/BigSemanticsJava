package ecologylab.semantics.library;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_nested;
import ecologylab.xml.ElementState.xml_tag;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Image extends Media
{
	@xml_nested MetadataString		caption;
	@xml_nested MetadataParsedURL	location;
	/**
	 * Occasionally, we want to navigate to somewhere other than the regular purl,
	 * as in when this is an RSS feed, but there's an equivalent HTML page.
	 */
	@xml_nested MetadataParsedURL	navLocation;

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
		if(result == null)
		{
			result 			= new MetadataString();
			this.caption 	= result;
		}
		return result;
	}
	MetadataParsedURL location()
	{
		MetadataParsedURL result = this.location;
		if(result == null)
		{
			result 			= new MetadataParsedURL();
			this.location 	= result;
		}
		return result;
	}
	MetadataParsedURL navLocation()
	{
		MetadataParsedURL result = this.navLocation;
		if(result == null)
		{
			result 			= new MetadataParsedURL();
			this.navLocation 	= result;
		}
		return result;
	}
	
	
	public String getCaption()
	{
//		return caption;
		return caption().getValue();
	}

	@Override
	public ParsedURL getLocation()
	{
//		return location;
		return location().getValue();
	}

	public void hwSetCaption(String caption)
	{
//		this.caption = caption;
		this.setCaption(caption);
		rebuildCompositeTermVector();
	}

	@Override
	public void hwSetLocation(ParsedURL location)
	{
//		this.location = location;
		this.setLocation(location);
		rebuildCompositeTermVector();
	}
	
	public void setCaption(String caption)
	{
//		this.caption = caption;
		this.caption().setValue(caption);
	}

	@Override
	public void setLocation(ParsedURL location)
	{
//		this.location = location;
		this.location().setValue(location);
	}
	/**
	 * @return the navLocation
	 */
	@Override
	public ParsedURL getNavLocation()
	{
//		return navLocation;
		return navLocation().getValue();
	}
	/**
	 * @param navLocation the navLocation to set
	 */
	@Override
	public void setNavLocation(ParsedURL navLocation)
	{
//		this.navLocation = navLocation;
		this.navLocation().setValue(navLocation);
	}

	@Override
	public void hwSetNavLocation(ParsedURL navLocation)
	{
//		this.navLocation = navLocation;
		this.setNavLocation(navLocation);
		rebuildCompositeTermVector();
	}
}
