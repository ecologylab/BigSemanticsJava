package ecologylab.semantics.library;

import ecologylab.model.TextChunkBase;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scalar.MetadataInteger;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.library.scalar.MetadataStringBuilder;
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
	@xml_nested MetadataString		title;
	@xml_nested MetadataString		description;
	@xml_nested MetadataParsedURL	location;
	
	@xml_nested MetadataStringBuilder 	anchorText;
	@xml_nested MetadataStringBuilder 	anchorContextString;
	
	@xml_nested MetadataInteger			generation;

	/**
	 * Occasionally, we want to navigate to somewhere other than the regular purl,
	 * as in when this is an RSS feed, but there's an equivalent HTML page.
	 */
	@xml_nested MetadataParsedURL	navLocation;
	
	public Document()
	{
	}
	
	
	public Document(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
	}
	
	//Efficient retrieval through lazy evaluation.
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
	
	MetadataStringBuilder anchorText()
	{
		MetadataStringBuilder result = this.anchorText;
		if(result == null)
		{
			result = new MetadataStringBuilder();
			this.anchorText = result;
		}
		return result;
	}

	MetadataStringBuilder anchorContextString()
	{
		MetadataStringBuilder result = this.anchorContextString;
		if(result == null)
		{
			result = new MetadataStringBuilder();
			this.anchorContextString = result;
		}
		return result;
	}

	MetadataInteger generation()
	{
		MetadataInteger result = this.generation;
		if(result == null)
		{
			result = new MetadataInteger();
			this.generation = result;
		}
		return result;
	}
	
	public String getTitle()
	{
		return title().getValue();
	}

	public String getDescription()
	{
		return description().getValue();
	}

	@Override
	public ParsedURL getLocation()
	{
		return location() != null ? location().getValue(): null;
	}
	
	@Override
	public ParsedURL getNavLocation()
	{
		return navLocation() != null ? navLocation().getValue(): null;
	}
	
	public void hwSetTitle(String title)
	{
		this.setTitle(title);
		rebuildCompositeTermVector();
	}

	public void hwSetDescription(String description)
	{
		this.setDescription(description);
		rebuildCompositeTermVector();
	}

	@Override
	public void hwSetLocation(ParsedURL location)
	{
		this.setLocation(location);
		rebuildCompositeTermVector();
	}
	
	@Override
	public void hwSetNavLocation(ParsedURL navLocation)
	{
		this.setNavLocation(navLocation);
		rebuildCompositeTermVector();
	}
	
	public void setTitle(String title)
	{
		this.title().setValue(title);
	}

	public void setDescription(String description)
	{
		this.description().setValue(description);
	}

	@Override
	public void setLocation(ParsedURL location)
	{
		this.location().setValue(location);
	}
	
	@Override
	public void setNavLocation(ParsedURL navLocation)
	{
		this.navLocation().setValue(navLocation);
	}

	public void setAnchorText(StringBuilder anchorText)
	{
		this.anchorText().setValue(anchorText);
	}

	public void hwSetAnchorText(StringBuilder anchorText)
	{
		this.anchorText().setValue(anchorText);
		rebuildCompositeTermVector();
	}
	
	public void appendAnchorText(String anchorText)
	{
		this.anchorText().setValue(anchorText);
	}
	
	public void appendAnchorContextString(String anchorContextString)
	{
		this.anchorContextString().setValue(anchorContextString);
	
	}
	public void hwAppendAnchorText(String anchorText)
	{
		this.anchorText().setValue(anchorText);
		rebuildCompositeTermVector();
	}
	public void hwAppendAnchorContextString(String anchorContextString)
	{
		this.anchorContextString().setValue(anchorContextString);
		rebuildCompositeTermVector();
	
	}

	public void setGeneration(int generation)
	{
		this.generation().setValue(generation);
	}



	
}

// Caption Images
// Title Documents
// Description Documents
// Anchor Documents
