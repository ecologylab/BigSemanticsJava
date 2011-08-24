package ecologylab.semantics.actions;

import ecologylab.semantics.collecting.Crawler;
import ecologylab.semantics.collecting.SemanticsSite;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.simpl_inherit;

/**
 * By default, this method prevent the InfoCollector from collecting information from the specified
 * domain, by calling the reject() method of the InfoCollector. You specify the unwanted domain by
 * providing the "domain" semantic argument in the meta-metadata xml codes.
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.BACK_OFF_FROM_SITE)
class BackOffFromSiteSemanticAction
		extends SemanticAction implements SemanticActionStandardMethods
{
	
	@simpl_scalar
	protected String domain;

	@Override
	public String getActionName()
	{
		return BACK_OFF_FROM_SITE;
	}

	@Override
	public void handleError()
	{
		// TODO
	}

	@Override
	public Object perform(Object obj)
	{
		if (domain != null)
		{
			SemanticsSite site	= sessionScope.getMetaMetadataRepository().getSite(domain);
			site.setAbnormallyLongNextAvailableTime();
			
//			debug("\t\t\tStep 2: Adding site to rejects");
//			infoCollector.reject(domain);
	
			debug("\t\t\tStep 2: Removing from current download Queues");
			sessionScope.getDownloadMonitors().killSite(site);
	
			debug("\t\t\tStep 3: Removing from candidate pools");
			Crawler crawler	= sessionScope.getCrawler();
			if (crawler != null)
				crawler.killSite(site);
		}
		return null;
	}

}
