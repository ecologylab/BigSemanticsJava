/**
 * 
 */
package ecologylab.semantics.library;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;

/**
 * @author bharat
 *
 */
@xml_inherit
public class Pdf extends Document
{
	@xml_attribute 	String			author; 	
	@xml_attribute  String			summary;
	@xml_attribute 	String			keywords;
	@xml_attribute	String 			subject;
	@xml_attribute	String			trapped;
	@xml_attribute 	String			modified;
	@xml_attribute	String 			contents;
	@xml_attribute	String			creationdate;
	/**
	 * @return the author
	 */
	public String getAuthor()
	{
		return author;
	}
	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author)
	{
		this.author = author;
		rebuildCompositeTermVector();
	}
	/**
	 * @param author the author to set
	 */
	public void lwSetAuthor(String author)
	{
		this.author = author;
	}
	/**
	 * @return the summary
	 */
	public String getSummary()
	{
		return summary;
	}
	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary)
	{
		this.summary = summary;
		rebuildCompositeTermVector();
	}
	/**
	 * @param summary the summary to set
	 */
	public void lwSetSummary(String summary)
	{
		this.summary = summary;
	}
	/**
	 * @return the keywords
	 */
	public String getKeywords()
	{
		return keywords;
	}
	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(String keywords)
	{
		this.keywords = keywords;
		rebuildCompositeTermVector();
	}
	/**
	 * @param keywords the keywords to set
	 */
	public void lwSetKeywords(String keywords)
	{
		this.keywords = keywords;
	}
	/**
	 * @return the subject
	 */
	public String getSubject()
	{
		return subject;
	}
	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject)
	{
		this.subject = subject;
		rebuildCompositeTermVector();
	}
	/**
	 * @param subject the subject to set
	 */
	public void lwSetSubject(String subject)
	{
		this.subject = subject;
	}
	/**
	 * @return the trapped
	 */
	public String getTrapped()
	{
		return trapped;
	}
	/**
	 * @param trapped the trapped to set
	 */
	public void setTrapped(String trapped)
	{
		this.trapped = trapped;
		rebuildCompositeTermVector();
	}
	/**
	 * @param trapped the trapped to set
	 */
	public void lwSetTrapped(String trapped)
	{
		this.trapped = trapped;
	}

}
