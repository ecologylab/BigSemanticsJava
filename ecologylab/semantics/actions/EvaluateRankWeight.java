/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * 
 * Semantic action to evualte weight basesd on rank.
 * @author amathur
 *
 */
@xml_inherit
public @xml_tag(SemanticActionStandardMethods.EVALUATE_RANK_WEIGHT)
class EvaluateRankWeight extends SemanticAction implements SemanticActionStandardMethods
{

	/* (non-Javadoc)
	 * @see ecologylab.semantics.actions.SemanticAction#getActionName()
	 */
	@Override
	public String getActionName()
	{
		return EVALUATE_RANK_WEIGHT;
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
