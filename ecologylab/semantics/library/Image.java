package ecologylab.semantics.library;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Image extends Media
{
	@xml_attribute	String 		caption;
	@xml_attribute	ParsedURL 	location;
	
	/**
	 * Occasionally, we want to navigate to somewhere other than the regular purl,
	 * as in when this is an RSS feed, but there's an equivalent HTML page.
	 */
	@xml_attribute	ParsedURL		navLocation;
	
	public Image()
	{
		
	}
	public Image(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
	}
	public String getCaption()
	{
		return caption;
	}

	@Override
	public ParsedURL getLocation()
	{
		return location;
	}

	public void hwSetCaption(String caption)
	{
		this.caption = caption;
		rebuildCompositeTermVector();
	}

	@Override
	public void hwSetLocation(ParsedURL location)
	{
		this.location = location;
		rebuildCompositeTermVector();
	}
	
	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	@Override
	public void setLocation(ParsedURL location)
	{
		this.location = location;
	}
	/**
	 * @return the navLocation
	 */
	@Override
	public ParsedURL getNavLocation()
	{
		return navLocation;
	}
	/**
	 * @param navLocation the navLocation to set
	 */
	@Override
	public void setNavLocation(ParsedURL navLocation)
	{
		this.navLocation = navLocation;
	}

	@Override
	public void hwSetNavLocation(ParsedURL navLocation)
	{
		this.navLocation = navLocation;
		rebuildCompositeTermVector();
	}
}
