/**
 * 
 */
package ecologylab.semantics.library.uva;

import java.util.ArrayList;

import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.library.Document;
import ecologylab.semantics.library.scalar.MetadataInteger;
import ecologylab.semantics.library.scalar.MetadataStringBuilder;

/**
 * @author vdeboer
 *
 */
public class Topic extends Document
{
	@xml_attribute MetadataInteger	id;
	
	MetadataStringBuilder			contentKeywords;
	MetadataStringBuilder			anchorKeywords;
	MetadataStringBuilder			titleKeywords;
	public MetadataInteger getId()
	{
		return id;
	}
	public void setId(MetadataInteger id)
	{
		this.id = id;
	}
	public MetadataStringBuilder getContentKeywords()
	{
		return contentKeywords;
	}
	public void setContentKeywords(MetadataStringBuilder contentKeywords)
	{
		this.contentKeywords = contentKeywords;
	}
	public MetadataStringBuilder getAnchorKeywords()
	{
		return anchorKeywords;
	}
	public void setAnchorKeywords(MetadataStringBuilder anchorKeywords)
	{
		this.anchorKeywords = anchorKeywords;
	}
	public MetadataStringBuilder getTitleKeywords()
	{
		return titleKeywords;
	}
	public void setTitleKeywords(MetadataStringBuilder titleKeywords)
	{
		this.titleKeywords = titleKeywords;
	}
	
//	@xml_collection("content_keywords") ArrayList<String> contentKeywords;
//	@xml_collection("anchor_keywords") ArrayList<String> anchorKeywords;
//	@xml_collection("title_keywords") ArrayList<String> titleKeywords;
//	@xml_collection("url_keywords") ArrayList<String> urlKeywords;
	
}
