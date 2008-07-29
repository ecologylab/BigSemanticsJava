/**
 * 
 */
package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.Document;
import ecologylab.semantics.library.Image;
import ecologylab.semantics.library.Media;
import ecologylab.semantics.library.Pdf;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_collection;
import ecologylab.xml.ElementState.xml_leaf;
import ecologylab.xml.ElementState.xml_map;
import ecologylab.xml.ElementState.xml_nested;
import ecologylab.xml.ElementState.xml_tag;

/**
 * 
 * @author bharat
 */
@xml_inherit
public class AcmPortal extends Pdf
{
	@xml_nested MetadataParsedURL						fullText;
	@xml_nested	Source									source;
	
	@xml_map("authors") 
	private HashMapArrayList<String, Author>			authorNames;
	
	@xml_tag("abstract")
	@xml_nested MetadataString							abst;
	@xml_map("references") 
	private HashMapArrayList<ParsedURL, Reference>		references;
	@xml_map("citations") 
	private HashMapArrayList<ParsedURL, Reference>		citations;

	
	
	public AcmPortal()
	{
		super();
	}
	
	public AcmPortal(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
	}

	//Lazy evaluation for efficient retrieval.
	MetadataString abst()
	{
		MetadataString result = this.abst;
		if(result == null)
		{
			result 			= new MetadataString();
			this.abst 	= result;
		}
		return result;
	}
	
	MetadataParsedURL fullText()
	{
		MetadataParsedURL result = this.fullText;
		if(result == null)
		{
			result 			= new MetadataParsedURL();
			this.fullText 	= result;
		}
		return result;
	}
	
	Source source()
	{
		Source result = this.source;
		if(result == null)
		{
			result 			= new Source();
			this.source 	= result;
		}
		return result;
	}
	
	HashMapArrayList<String, Author> author()
	{
		HashMapArrayList<String, Author> result = this.authorNames;
		if(result == null)
		{
			result 			= new HashMapArrayList<String, Author>();
			this.authorNames 	= result;
		}
		return result;
	}
	
	HashMapArrayList<ParsedURL, Reference> references()
	{
		HashMapArrayList<ParsedURL, Reference> result = this.references;
		if(result == null)
		{
			result 				= new HashMapArrayList<ParsedURL, Reference>();
			this.references 	= result;
		}
		return result;
	}
	
	HashMapArrayList<ParsedURL, Reference> citations()
	{
		HashMapArrayList<ParsedURL, Reference> result = this.citations;
		if(result == null)
		{
			result 				= new HashMapArrayList<ParsedURL, Reference>();
			this.citations 	= result;
		}
		return result;
	}
	
	public Source getSource()
	{
		return source();
	}

	public void setSource(Source source)
	{
		this.source = source;
	}

	public String getTitle()
	{
		return super.getTitle();
	}

	public void setTitle(String title)
	{
		super.setTitle(title);
	}

	public ParsedURL getFullText()
	{
		return fullText().getValue();
	}

}
