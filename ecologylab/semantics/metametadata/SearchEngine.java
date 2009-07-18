/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.net.ParsedURL;
import ecologylab.xml.ElementState;
import ecologylab.xml.types.element.Mappable;

/**
 * @author amathur
 *
 */
public class SearchEngine extends ElementState implements Mappable<String>
{

	@xml_attribute private String name;
	
	@xml_attribute private String urlPrefix;
	
	@xml_attribute private String urlSuffix;
	
	@xml_attribute private String numResultString;
	
	@xml_attribute private String startString;
	
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
	
}
