package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.types.element.IMappable;

/**
 * 
 * @author rhema Contains all of the information that one should need to define when a particular
 *         MetaMetadata object should be selected.
 * 
 */

public @simpl_inherit
class MetaMetadataSelector extends ElementState implements IMappable<String>
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
	
	@simpl_scalar
	private Pattern											urlRegexFragment;

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
	
	/* followings are used for selecting another meta-metadata according to a particular field */
	
	@simpl_scalar
	@simpl_tag("meta_metadata_name")
	private String 											reselectMetaMetadataName;
	
	@simpl_nowrap
	@simpl_map("field")
	private HashMapArrayList<String, MetaMetadataSelectorReselectField> reselectFields;
	
	@simpl_nowrap
	@simpl_collection("param")
	private ArrayList<MetaMetadataSelectorParam>         params;
	
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
	
	public Pattern getUrlRegexFragment()
	{
		return urlRegexFragment;
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
	
	public String getReselectMetaMetadataName()
	{
		return reselectMetaMetadataName;
	}

	public boolean reselect(Metadata metadata)
	{
		if (reselectFields == null || reselectFields.isEmpty())
			return false;
		
		for (String fieldName : reselectFields.keySet())
		{
			FieldDescriptor fd = metadata.getMetadataClassDescriptor().getFieldDescriptorByFieldName(fieldName);
			String actualValue = fd.getValueString(metadata);
			String expectedValue = reselectFields.get(fieldName).getValue();
			if (!actualValue.equals(expectedValue))
				return false;
		}
		return true;
	}
	
	public List<MetaMetadataSelectorParam> getParams()
	{
	  return params;
	}
	
	void addParam(MetaMetadataSelectorParam param)
	{
	  if (params == null)
	    params = new ArrayList<MetaMetadataSelectorParam>();
	  params.add(param);
	}
	
	public boolean checkForParams(final ParsedURL purl)
	{
	  if (params != null)
	  {
      HashMap<String, String> purlParams = purl.extractParams(true);
	    for (MetaMetadataSelectorParam param : params)
	    {
	      String paramName = param.getName();
	      String paramValue = param.getValue();
	      String paramValueIsNot = param.getValueIsNot();
	      String actualValue = purlParams == null ? null : purlParams.get(paramName);
        if (actualValue == null)
        {
          actualValue = "";
        }

	      if (paramValue != null && paramValue.length() > 0)
	      {
	        
	        boolean allowEmptyAndIsEmpty = param.isAllowEmptyValue() && actualValue.length() == 0;
	        if (!allowEmptyAndIsEmpty && !paramValue.equals(actualValue))
	        {
	          return false;
	        }
	      }

	      if (paramValueIsNot != null && paramValueIsNot.length() > 0)
	      {
	        if (paramValueIsNot.equals(actualValue))
	        {
	          return false;
	        }
	      }
	    }
	  }
	  return true;
	}
	
}
