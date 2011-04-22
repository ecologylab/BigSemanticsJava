package ecologylab.semantics.metametadata;

import java.util.ArrayList;
import java.util.regex.Pattern;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.element.Mappable;

/**
 * 
 * @author rhema Contains all of the information that one should need to define when a particular
 *         MetaMetadata object should be selected.
 * 
 */

public @simpl_inherit
class MetaMetadataSelector extends ElementState implements Mappable<String>
{
	public static final ArrayList<MetaMetadataSelector> NULL_SELECTOR = new ArrayList<MetaMetadataSelector>();
	
	@simpl_scalar
	private String											name;

	@simpl_scalar
	private String											prefName;
	
	@simpl_scalar
	private String											defaultPref;

	@simpl_scalar
	private ParsedURL										urlStripped;

	@simpl_scalar
	private ParsedURL										urlPathTree;

	/**
	 * Regular expression. Must be paired with domain. This is the least efficient form of matcher, so
	 * it should be used only when url_base & url_prefix cannot be used.
	 */
	@simpl_scalar
	private Pattern											urlRegex;

	/**
	 * This key is *required* for urlPatterns, so that we can organize them efficiently.
	 */
	@simpl_scalar
	private String											domain;

	@simpl_collection("mime_type")
	@simpl_nowrap
	private ArrayList<String>						mimeTypes;

	@simpl_collection("suffix")
	@simpl_nowrap
	private ArrayList<String>						suffixes;

	public MetaMetadataSelector()
	{

	}

	public ParsedURL getUrlStripped()
	{
		return urlStripped;
	}

	public void setUrlStripped(ParsedURL urlStripped)
	{
		this.urlStripped = urlStripped;
	}

	public ParsedURL getUrlPathTree()
	{
		return urlPathTree;
	}

	public void setUrlPathTree(ParsedURL urlPathTree)
	{
		this.urlPathTree = urlPathTree;
	}

	public Pattern getUrlRegex()
	{
		return urlRegex;
	}

	public void setUrlRegex(Pattern urlRegex)
	{
		this.urlRegex = urlRegex;
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
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

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setPrefName(String cfPref)
	{
		this.prefName = cfPref;
	}

	public String getPrefName()
	{
		return prefName;
	}

	@Override
	public String key()
	{
		return name;
	}
	
	public String getDefaultPref()
	{
		return defaultPref;
	}

	public void setDefaultPref(String defaultPref)
	{
		this.defaultPref = defaultPref;
	}
}
