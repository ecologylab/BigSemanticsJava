package ecologylab.semantics.actions;

import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

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
		sessionScope.getSeeding().reject(domain);
		return null;
	}

}
