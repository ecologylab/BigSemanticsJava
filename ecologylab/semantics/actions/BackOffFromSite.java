package ecologylab.semantics.actions;

import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * 
 * 
 * @author damaraju
 *
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.BACK_OFF_FROM_SITE)
class BackOffFromSite extends SemanticAction implements SemanticActionStandardMethods
{

	/* (non-Javadoc)
	 * @see ecologylab.semantics.actions.SemanticAction#getActionName()
	 */
	@Override
	public String getActionName()
	{
		return BACK_OFF_FROM_SITE;
	}

	/* (non-Javadoc)
	 * @see ecologylab.semantics.actions.SemanticAction#handleError()
	 */
	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

}
