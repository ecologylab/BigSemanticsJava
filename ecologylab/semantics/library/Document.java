package ecologylab.semantics.library;

import ecologylab.model.TextChunkBase;
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
public class Document extends Metadata
{
	@xml_attribute	String 			title;
	@xml_attribute	String 			description;
	@xml_attribute	ParsedURL 		location;
	
	/**
	 * Occasionally, we want to navigate to somewhere other than the regular purl,
	 * as in when this is an RSS feed, but there's an equivalent HTML page.
	 */
	@xml_attribute	ParsedURL		navLocation;
	
	//Metadata TransitionTODO -- In PDFTypeMultiAndBox the following are used...shall i create a new class or shall i keep them here??
//	@xml_attribute 	String			author; 	
//	@xml_attribute  String			summary;
//	@xml_attribute 	String			keywords;
//	@xml_attribute	String 			subject;
//	@xml_attribute	String			trapped;
	
	
	public Document()
	{
		
	}
	
	public Document(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
	}
	
	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	@Override
	public ParsedURL getLocation()
	{
		return location;
	}
	
	@Override
	public ParsedURL getNavLocation()
	{
		return navLocation;
	}
	
	public void hwSetTitle(String title)
	{
		this.title = title;
		rebuildCompositeTermVector();
	}

	public void hwSetDescription(String description)
	{
		this.description = description;
		rebuildCompositeTermVector();
	}

	@Override
	public void hwSetLocation(ParsedURL location)
	{
		this.location = location;
		rebuildCompositeTermVector();
	}
	
	@Override
	public void hwSetNavLocation(ParsedURL navLocation)
	{
		this.navLocation = navLocation;
		rebuildCompositeTermVector();
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public void setLocation(ParsedURL location)
	{
		this.location = location;
	}
	
	@Override
	public void setNavLocation(ParsedURL navLocation)
	{
		this.navLocation = navLocation;
	}

}

// Caption Images
// Title Documents
// Description Documents
// Anchor Documents
