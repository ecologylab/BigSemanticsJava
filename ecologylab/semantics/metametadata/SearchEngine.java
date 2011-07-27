/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.types.element.Mappable;

/**
 * @author amathur
 *
 */
public class SearchEngine extends ElementState implements Mappable<String>
{

	@simpl_scalar private String name;
	
	@simpl_scalar private String urlPrefix;
	
	@simpl_scalar private String urlSuffix;
	
	@simpl_scalar private String numResultString;
	
	@simpl_scalar private String startString;
	
	@simpl_scalar private boolean homogeneous;
	
	//these are used in the cf web interface
	
	@simpl_scalar private String group;
	
	@simpl_scalar private String description;
	
	@simpl_scalar private String favicon;
	
	@simpl_scalar private boolean show;
	
	public SearchEngine() {}

	/**
	 * @return the name
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the url
	 */
	public final String getUrlPrefix()
	{
		return urlPrefix;
	}

	/**
	 * @param url the url to set
	 */
	public final void setUrlPrefix(String urlPrefix)
	{
		this.urlPrefix = urlPrefix;
	}


	public String key()
	{
		// TODO Auto-generated method stub
		return name;
	}

	/**
	 * @return the urlSuffix
	 */
	public final String getUrlSuffix()
	{
		if(urlSuffix!=null)
			return urlSuffix;
		else
			return "";
	}

	/**
	 * @param urlSuffix the urlSuffix to set
	 */
	public final void setUrlSuffix(String urlSuffix)
	{
		this.urlSuffix = urlSuffix;
	}

	/**
	 * @return the numResultString
	 */
	public final String getNumResultString()
	{
		if(numResultString!=null)
			return numResultString;
		else
			return "";
	}

	/**
	 * @param numResultString the numResultString to set
	 */
	public final void setNumResultString(String numResultString)
	{
		this.numResultString = numResultString;
	}

	/**
	 * @return the startString
	 */
	public final String getStartString()
	{
		if(startString!=null)
			return startString;
		else
			return "";
	}

	/**
	 * @param startString the startString to set
	 */
	public final void setStartString(String startString)
	{
		this.startString = startString;
	}
	
	public ParsedURL formSearchUrl(String query, int numResults, int currentFirstResultIndex)
	{
		query = query.replace(' ', '+');
		
		StringBuilder url = new StringBuilder(urlPrefix);
		url.append(query);
		if (!StringTools.isNullOrEmpty(numResultString) && numResults > 0)
			url.append(numResultString).append(numResults);
		if (!StringTools.isNullOrEmpty(startString) && currentFirstResultIndex > 0)
			url.append(startString).append(currentFirstResultIndex);
		if (!StringTools.isNullOrEmpty(urlSuffix))
			url.append(urlSuffix);
		
		return ParsedURL.getAbsolute(url.toString());
	}
	
}
