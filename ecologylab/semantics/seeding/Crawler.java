/**
 * 
 */
package ecologylab.semantics.seeding;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.InfoCollector;

/**
 * Specification of a directive to the agent or otherwise to compositon space services.
 * 
 * Version for client only:
 * 	<li>data slot definitions only with no other functionality.</li>
 * 
 * @author andruid
 */
public class Crawler extends Seed
{
	/**
	 * URL that the action operates on.
	 */
	@simpl_scalar
	protected					ParsedURL	url;
	
	/**
	 * The domain -- for reject actions only.
	 */
	@simpl_scalar
	protected					String		domain;
	
	/**
	 * What is the web crawler being told to do?
	 * 		traversable, untraversable, or reject.
	 */
	@simpl_scalar
	protected					String		action;

	/**
	 * @return the url
	 */
	public ParsedURL getUrl()
	{
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(ParsedURL url)
	{
		this.url = url;
	}

	/**
	 * @return the domain
	 */
	public String getDomain()
	{
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	/**
	 * @return the action
	 */
	public String getAction()
	{
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action)
	{
		this.action = action;
	}

	@Override
	public String categoryString()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String detailedCategoryString()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEditable()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void performInternalSeedingSteps(InfoCollector infoCollector)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCategory(String value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setValue(String value)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String valueString()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
