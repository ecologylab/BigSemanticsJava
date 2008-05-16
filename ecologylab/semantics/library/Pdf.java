/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_nested;

/**
 * @author bharat
 *
 */
@xml_inherit
public class Pdf extends Document
{
//	@xml_attribute 	String			author; 	
//	@xml_attribute  String			summary;
//	@xml_attribute 	String			keywords;
//	@xml_attribute	String 			subject;
//	//TODO -- get rid of this
//	@xml_attribute	String			trapped;
//	@xml_attribute 	String			modified;
//	//TODO -- what is this? why do we have it?
//	@xml_attribute	String 			contents;
//	@xml_attribute	String			creationdate;
	
	
	@xml_nested MetadataString	author = new MetadataString();
	@xml_nested MetadataString	summary = new MetadataString();
	@xml_nested MetadataString	keywords = new MetadataString();
	@xml_nested MetadataString	subject = new MetadataString();
	@xml_nested MetadataString	trapped = new MetadataString();
	@xml_nested MetadataString	modified = new MetadataString();
	@xml_nested MetadataString	contents = new MetadataString();
	@xml_nested MetadataString	creationdate = new MetadataString();
	
	MetadataString author()
	{
		MetadataString result = this.author;
		if(result == null)
		{
			result 			= new MetadataString();
			this.author 	= result;
		}
		return result;
	}
	MetadataString summary()
	{
		MetadataString result = this.summary;
		if(result == null)
		{
			result 			= new MetadataString();
			this.summary 	= result;
		}
		return result;
	}
	MetadataString keywords()
	{
		MetadataString result = this.keywords;
		if(result == null)
		{
			result 			= new MetadataString();
			this.keywords 	= result;
		}
		return result;
	}
	MetadataString subject()
	{
		MetadataString result = this.subject;
		if(result == null)
		{
			result 			= new MetadataString();
			this.subject 	= result;
		}
		return result;
	}
	MetadataString trapped()
	{
		MetadataString result = this.trapped;
		if(result == null)
		{
			result 			= new MetadataString();
			this.trapped 	= result;
		}
		return result;
	}
	MetadataString modified()
	{
		MetadataString result = this.modified;
		if(result == null)
		{
			result 			= new MetadataString();
			this.modified 	= result;
		}
		return result;
	}
	MetadataString contents()
	{
		MetadataString result = this.contents;
		if(result == null)
		{
			result 			= new MetadataString();
			this.contents 	= result;
		}
		return result;
	}
	MetadataString creationdate()
	{
		MetadataString result = this.creationdate;
		if(result == null)
		{
			result 			= new MetadataString();
			this.creationdate 	= result;
		}
		return result;
	}
	
	
	/**
	 * @return the author
	 */
	public String getAuthor()
	{
//		return author;
		return author().getValue();
	}
	/**
	 * @param author the author to set
	 */
	public void hwSetAuthor(String author)
	{
//		this.author = author;
		this.author().setValue(author);
		rebuildCompositeTermVector();
	}
	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author)
	{
//		this.author = author;
		this.author().setValue(author);
	}
	/**
	 * @return the summary
	 */
	public String getSummary()
	{
//		return summary;
		return summary().getValue();
	}
	/**
	 * @param summary the summary to set
	 */
	public void hwSetSummary(String summary)
	{
//		this.summary = summary;
		this.summary().setValue(summary);
		rebuildCompositeTermVector();
	}
	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary)
	{
//		this.summary = summary;
		this.summary().setValue(summary);
	}
	/**
	 * @return the keywords
	 */
	public String getKeywords()
	{
//		return keywords;
		return keywords().getValue();
	}
	/**
	 * @param keywords the keywords to set
	 */
	public void hwSetKeywords(String keywords)
	{
//		this.keywords = keywords;
		this.keywords().setValue(keywords);
		rebuildCompositeTermVector();
	}
	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(String keywords)
	{
//		this.keywords = keywords;
		this.keywords().setValue(keywords);
	}
	/**
	 * @return the subject
	 */
	public String getSubject()
	{
//		return subject;
		return subject().getValue();
	}
	/**
	 * @param subject the subject to set
	 */
	public void hwSetSubject(String subject)
	{
//		this.subject = subject;
		this.subject().setValue(subject);
		rebuildCompositeTermVector();
	}
	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject)
	{
//		this.subject = subject;
		this.subject().setValue(subject);
	}
	/**
	 * @return the trapped
	 */
	public String getTrapped()
	{
//		return trapped;
		return trapped().getValue();
	}
	/**
	 * @param trapped the trapped to set
	 */
	public void hwSetTrapped(String trapped)
	{
//		this.trapped = trapped;
		this.trapped().setValue(trapped);
		rebuildCompositeTermVector();
	}
	/**
	 * @param trapped the trapped to set
	 */
	public void setTrapped(String trapped)
	{
//		this.trapped = trapped;
		this.trapped().setValue(trapped);
	}

}
