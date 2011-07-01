/**
 * 
 */
package ecologylab.semantics.collecting;

import ecologylab.collections.Scope;

/**
 * The SemanticsSessionScope will include references to Crossroads and to Crawler, if there is one. 
 * I believe it is also where we store state related to Seeding. 
 * A subclass, InteractiveSemanticsSessionScope, will also include a reference to the AWTBridge. 
 * A likely further subclass is CfSemanticsSessionScope.
 * 
 * @author andruid
 */
public class SemanticsSessionScope extends Scope<Object>
{
	private NewInfoCollector	crossroads;
	
	private Crawler						crawler;
	
	public SemanticsSessionScope(NewInfoCollector	crossroads,  Crawler	crawler)
	{
		super();
		this.crossroads	= crossroads;
		this.crawler		= crawler;
	}
	public SemanticsSessionScope(NewInfoCollector	crossroads)
	{
		this(crossroads, null);
	}
	
	
}
