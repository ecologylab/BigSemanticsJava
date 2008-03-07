package ecologylab.semantics.library;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.xml_inherit;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Image extends Metadata
{
	@xml_attribute	String caption;
	@xml_attribute	ParsedURL location;
	
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

	public ParsedURL getLocation()
	{
		return location;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
		rebuildCompositeTermVector();
	}

	public void setLocation(ParsedURL location)
	{
		this.location = location;
		rebuildCompositeTermVector();
	}
	
	public void setLwCaption(String caption)
	{
		this.caption = caption;
	}

	public void setLwLocation(ParsedURL location)
	{
		this.location = location;
	}

}
