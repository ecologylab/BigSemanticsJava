/**
 * 
 */
package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.Document;
import ecologylab.semantics.library.Image;
import ecologylab.semantics.library.Media;
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
 * @author bharat
 *
 */
@xml_inherit
public class AcmPortal extends Document
{
	@xml_nested MetadataParsedURL										fullText;
	@xml_nested	Source													source 		= new Source();
	@xml_map("author") 
	public 	HashMapArrayList<String, Author>							author 		= new HashMapArrayList<String, Author>();;
	@xml_tag("abstract")
		@xml_nested MetadataString										abst;
	@xml_map("references") 
		private HashMapArrayList<ParsedURL, Reference>					references  = new HashMapArrayList<ParsedURL, Reference>();
	@xml_map("citations") 
		private HashMapArrayList<ParsedURL, Reference>					citations   = new HashMapArrayList<ParsedURL, Reference>();

	
	//The title is already there in Document
//	@xml_nested MetadataString											title = new MetadataString();
//	@xml_nested MetadataParsedURL										fullText = new MetadataParsedURL();
	
//	@xml_leaf 	String														title;
//	@xml_leaf	ParsedURL 													fullText;
//	@xml_nested	Source														source 		= new Source();
//	@xml_map("author") 
//	public 	HashMapArrayList<String, Author>							author 		= new HashMapArrayList<String, Author>();
	
//	@xml_map("author")
//	@xml_map private AuthorsMap author = new AuthorsMap();
	
	
//	@xml_tag("abstract")
////	@xml_leaf 	String														abst;
//	@xml_nested MetadataString												abst = new MetadataString();
//	@xml_map("references") 
//	private HashMapArrayList<ParsedURL, Reference>							references 	= new HashMapArrayList<ParsedURL, Reference>();
//	@xml_map("citations") 
//	private HashMapArrayList<ParsedURL, Reference>							citations 	= new HashMapArrayList<ParsedURL, Reference>();
	
//	@xml_map("references") 
//	private ReferencesMap references = new ReferencesMap();
//	@xml_map("citations")
//	private ReferencesMap citations = new ReferencesMap();
	
	public AcmPortal(MetaMetadata metaMetadata)
	{
		super(metaMetadata);
	}

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
		HashMapArrayList<String, Author> result = this.author;
		if(result == null)
		{
			result 			= new HashMapArrayList<String, Author>();
			this.author 	= result;
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
	
	/**
	 * @return the source
	 */
	public Source getSource()
	{
		return source();
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Source source)
	{
		this.source = source;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return super.getTitle();
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title)
	{
		super.setTitle(title);
	}
}
