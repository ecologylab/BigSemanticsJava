package ecologylab.semantics.metametadata;

import java.util.ArrayList;
import java.util.regex.Pattern;

import ecologylab.net.ParsedURL;
import ecologylab.xml.ElementState;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_collection;
import ecologylab.xml.ElementState.xml_nowrap;
import ecologylab.xml.ElementState.xml_tag;

/**
 * 
 * @author rhema
 *  Contains all of the information that one should need to define
 *  when a particular MetaMetadata object should be selected.
 *
 */

public @xml_inherit class MetaMetadataSelector extends ElementState{

	@xml_attribute
	private String					name;
	
	@xml_attribute
	private String					cfPref;
	//TODO add somthing to make cfPref do what it is supposed to.  Cf pref could generate a static int that creates an ordering...
	
	@xml_attribute
	private ParsedURL					urlStripped;
	
	@xml_attribute
	private ParsedURL 				urlPathTree;

	/**
	 * Regular expression. Must be paired with domain.
	 * This is the least efficient form of matcher, so it should be used only when url_base & url_prefix cannot be used.
	 */
	@xml_attribute
	private Pattern						urlRegex;
	
	/**
	 * This key is *required* for urlPatterns, so that we can organize them efficiently.
	 */
	@xml_attribute
	private String						domain;
	
	@xml_collection("mime_type")
	@xml_nowrap 
	private ArrayList<String>	mimeTypes;

	@xml_collection("suffix")
	@xml_nowrap 
	private ArrayList<String>	suffixes;
	
	public MetaMetadataSelector()
	{
		System.out.println("selectorcreted");
	}
	
	public ParsedURL getUrlStripped() {
		return urlStripped;
	}

	public void setUrlStripped(ParsedURL urlStripped) {
		this.urlStripped = urlStripped;
	}

	public ParsedURL getUrlPathTree() {
		return urlPathTree;
	}

	public void setUrlPathTree(ParsedURL urlPathTree) {
		this.urlPathTree = urlPathTree;
	}

	public Pattern getUrlRegex() {
		return urlRegex;
	}

	public void setUrlRegex(Pattern urlRegex) {
		this.urlRegex = urlRegex;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}


	public ParsedURL getUrlBase()
	{
		return urlStripped;
	}

	public void setUrlBase(ParsedURL urlBase)
	{
		this.urlStripped = urlBase;
	}

	public void setUrlBase(String urlBase)
	{
		this.urlStripped = ParsedURL.getAbsolute(urlBase);
	}

	public ParsedURL getUrlPrefix()
	{
		return urlPathTree;
	}
	
	public void setUrlPrefix(ParsedURL urlPrefix)
	{
		this.urlPathTree = urlPrefix;
	}
	
	public void setUrlPrefix(String urlPrefix)
	{
		this.urlPathTree = ParsedURL.getAbsolute(urlPrefix);
	}
	/**
	 * @param mimeTypes
	 *          the mimeTypes to set
	 */
	public void setMimeTypes(ArrayList<String> mimeTypes)
	{
		this.mimeTypes = mimeTypes;
	}

	/**
	 * @return the mimeTypes
	 */
	public ArrayList<String> getMimeTypes()
	{
		return mimeTypes;
	}

	/**
	 * @param suffixes
	 *          the suffixes to set
	 */
	public void setSuffixes(ArrayList<String> suffixes)
	{
		this.suffixes = suffixes;
	}

	/**
	 * @return the suffixes
	 */
	public ArrayList<String> getSuffixes()
	{
		return suffixes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setCfPref(String cfPref) {
		this.cfPref = cfPref;
	}

	public String getCfPref() {
		return cfPref;
	}


	
}
