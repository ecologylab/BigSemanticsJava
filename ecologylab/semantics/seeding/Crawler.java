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

	public Crawler()
	{
		super();
	}
	
	public Crawler(String value, String action)
	{
		super();
		this.setValue(value);
		this.setCategory(action);
	}
	
	/**
	 * Check the validity of this seed.
	 */
	public boolean validate()
	{
		boolean result = false;
		if (REJECT.equals(action))
		{
			String arg	= domain;
			if (arg == null)
			{
				if (url != null)
				{
					arg	= url.domain();
					domain	= arg;
				}
			}
			if (arg != null)
				result = true;
		}

		else if (url != null)
			result = true;
		
		return result;
	}
	
	
	/**
	 * Bring this seed into the agent or directly into the composition.
	 * 
	 * @param infoCollector 
	 */
	public void performInternalSeedingSteps(InfoCollector infoCollector)
 	{
  	   if (REJECT.equals(action))
  		   infoCollector.reject(domain);

  	   else if (url != null)
 	   {
		   if (TRAVERSABLE.equals(action))
			   infoCollector.traversable(url);

		   else if (UNTRAVERSABLE.equals(action))
			   infoCollector.untraversable(url);
 	   }
 	}

	/**
	 * Set the value of the purl field, if the String is valid, 
	 * or (assume) and set the value to be the domain.
	 * 
	 * @param value
	 */
	public boolean setValue(String value)
	{
		ParsedURL trialValue	= ParsedURL.getAbsolute(value, "error parsing from seed");
		boolean result			= (trialValue != null);
		if (result)
			url					= trialValue;
		else
		{
			domain				= value;
			debug("Assuming value from seed is the domain");
		}
		return true;
	}
 	
 	/**
	 * The String the dashboard needs to show.
	 * 
	 * @return	The search query.
	 */
	public String valueString()
	{
		return (domain != null) ? domain : url.toString();
	}
	
	/**
	 * The category the dashboard uses to show.
	 * 
	 * @return	The search category.
	 */
	public String categoryString()
	{
		return (action != null) ? action : new String("");
	}

	/**
	 * @param actionTypeString
	 */
	public void setCategory(String actionTypeString)
	{
		action = actionTypeString;
	}
	
	/**
	 * Not used but necessary.
	 * 
	 * @return	The search engine category.
	 */
	public String detailedCategoryString()
	{
		return (action != null) ? action : new String("");
	}
	
	public ParsedURL purl()
	{
		return url;
	}

	public boolean canChangeVisibility()
	{
		return true;
	}

	public boolean isDeletable()
	{
		return true;
	}

	public boolean isEditable()
	{
		return true;
	}

	public boolean isRejectable()
	{
		return true;
	}


	
}
