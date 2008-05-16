package ecologylab.semantics.library;

import ecologylab.model.TextChunkBase;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_leaf;
import ecologylab.xml.ElementState.xml_nested;

/**
 * 
 * @author damaraju
 *
 */

@xml_inherit
public class Document extends Metadata
{
//	@xml_attribute	String 			title;
//	@xml_attribute	String 			description;
//	@xml_attribute	ParsedURL 		location;
	
//	@xml_nested MetadataString		title = new MetadataString();
//	@xml_nested MetadataString		description = new MetadataString();
//	@xml_nested MetadataParsedURL	location = new MetadataParsedURL();

	
	@xml_nested MetadataString		title;
	@xml_nested MetadataString		description;
	@xml_nested MetadataParsedURL	location;
	
	//Metdata Transition: debug
//	@xml_attribute	String context;
//	@xml_nested MetadataString		context = new MetadataString();
	@xml_nested MetadataString		context;
	
	/**
	 * Occasionally, we want to navigate to somewhere other than the regular purl,
	 * as in when this is an RSS feed, but there's an equivalent HTML page.
	 */
//	@xml_attribute	ParsedURL		navLocation;
//	@xml_nested MetadataParsedURL	navLocation = new MetadataParsedURL();
	@xml_nested MetadataParsedURL	navLocation;
	
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
	
	MetadataString title()
	{
		MetadataString result = this.title;
		if(result == null)
		{
			result = new MetadataString();
			this.title = result;
		}
		return result;
	}
	
	MetadataString description()
	{
		MetadataString result = this.description;
		if(result == null)
		{
			result = new MetadataString();
			this.description = result;
		}
		return result;
	}
	MetadataParsedURL location()
	{
		MetadataParsedURL result = this.location;
		if(result == null)
		{
			result = new MetadataParsedURL();
			this.location = result;
		}
		return result;
	}
	MetadataString context()
	{
		MetadataString result = this.context;
		if(result == null)
		{
			result = new MetadataString();
			this.context = result;
		}
		return result;
	}
	MetadataParsedURL navLocation()
	{
		MetadataParsedURL result = this.navLocation;
		if(result == null)
		{
			result = new MetadataParsedURL();
			this.navLocation = result;
		}
		return result;
	}
	
	public String getTitle()
	{
//		return title;
		return title().getValue();
	}

	public String getDescription()
	{
//		return description;
		return description().getValue();
	}

	@Override
	public ParsedURL getLocation()
	{
//		return location;
		return location() != null ? location().getValue(): null;
	}
	
	@Override
	public ParsedURL getNavLocation()
	{
//		return navLocation;
		return navLocation() != null ? navLocation().getValue(): null;
	}
	
	public void hwSetTitle(String title)
	{
//		this.title = title;
		this.title().setValue(title);
		rebuildCompositeTermVector();
	}

	public void hwSetDescription(String description)
	{
//		this.description = description;
		this.description().setValue(description);
		rebuildCompositeTermVector();
	}

	@Override
	public void hwSetLocation(ParsedURL location)
	{
//		this.location = location;
		this.location().setLocation(location);
		rebuildCompositeTermVector();
	}
	
	@Override
	public void hwSetNavLocation(ParsedURL navLocation)
	{
//		this.navLocation = navLocation;
		this.navLocation().setValue(navLocation);
		rebuildCompositeTermVector();
	}
	
	public void setTitle(String title)
	{
//		this.title = title;
		this.title().setValue(title);
	}

	public void setDescription(String description)
	{
//		this.description = description;
		this.description().setValue(description);
	}

	@Override
	public void setLocation(ParsedURL location)
	{
//		this.location = location;
		this.location().setValue(location);
	}
	
	@Override
	public void setNavLocation(ParsedURL navLocation)
	{
//		this.navLocation = navLocation;
		this.navLocation().setValue(navLocation);
	}

}

// Caption Images
// Title Documents
// Description Documents
// Anchor Documents
