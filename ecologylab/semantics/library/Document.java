package ecologylab.semantics.library;

import ecologylab.model.TextChunkBase;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;

/**
 * 
 * @author damaraju
 *
 */
public class Document extends Metadata
{
	@xml_attribute	String 			title;
	@xml_attribute	String 			description;
	@xml_attribute	ParsedURL 		location;
	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	public ParsedURL getLocation()
	{
		return location;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setLocation(ParsedURL location)
	{
		this.location = location;
	}

}

// Caption Images
// Title Documents
// Description Documents
// Anchor Documents
