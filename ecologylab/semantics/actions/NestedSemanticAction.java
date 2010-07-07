/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;

import ecologylab.xml.simpl_inherit;
import ecologylab.xml.ElementState.simpl_nowrap;

/**
 * This class is the base class for semantic actions which can have nested semantic actions inside
 * them. Right now only FOREACH and IF semantic actions can have other semantic actions nested
 * inside them.
 * 
 * @author amathur
 * 
 */

@simpl_inherit
public abstract class NestedSemanticAction<SA extends SemanticAction> extends SemanticAction
{

	/**
	 * List of nested semantic actions.
	 */
	@simpl_nowrap 
	@simpl_collection
	@simpl_scope(NestedSemanticActionsTranslationScope.NESTED_SEMANTIC_ACTIONS_SCOPE)
	private ArrayList<SA>	nestedSemanticActionList;

	/**
	 * @return the nestedSemanticActionList
	 */
	public ArrayList<SA> getNestedSemanticActionList()
	{
		return nestedSemanticActionList;
	}

	/**
	 * @param nestedSemanticActionList
	 *          the nestedSemanticActionList to set
	 */
	public void setNestedSemanticActionList(ArrayList<SA> nestedSemanticActionList)
	{
		this.nestedSemanticActionList = nestedSemanticActionList;
	}
}
