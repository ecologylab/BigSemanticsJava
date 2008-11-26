/**
 * 
 */
package ecologylab.semantics.library.scholarlyPublication;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.Pdf;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;

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

	public static void main(String[] args)
	{
		AcmPortal portal = new AcmPortal();
		Reference ref 	 = new Reference();
		Reference cit		 = new Reference();
		ref.bibTex = new MetadataString();
		ref.bibTex.setValue("Ref 1");
		portal.references = new HashMapArrayList<ParsedURL, Reference>();
		portal.references.put(ParsedURL.getAbsolute("http://www.portal.com"), ref);
		cit.bibTex = new MetadataString();
		cit.bibTex.setValue("Cit 1");
		portal.citations = new HashMapArrayList<ParsedURL, Reference>();
		portal.citations.put(ParsedURL.getAbsolute("http://www.portalCitation.com"), cit);
		
		try
		{
			portal.translateToXML(System.out);
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
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

	public void setAuthors(HashMapArrayList<String, Author> authors)
	{
		this.authorNames = authors;
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
