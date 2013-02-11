/**
 * 
 */
package ecologylab.bigsemantics.actions;

import java.util.ArrayList;

import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scope;

/**
 * This class is the base class for semantic actions which can have nested semantic actions inside
 * them. Right now only FOREACH and IF semantic actions can have other semantic actions nested
 * inside them.
 * 
 * @author amathur
 * 
 */

@simpl_inherit
public abstract class NestedSemanticAction
		extends SemanticAction
{

	/**
	 * List of nested semantic actions.
	 */
	@simpl_nowrap
	@simpl_collection
	@simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
	private ArrayList<SemanticAction>	nestedSemanticActionList;

	/**
	 * @return the nestedSemanticActionList
	 */
	public ArrayList<SemanticAction> getNestedSemanticActionList()
	{
		return nestedSemanticActionList;
	}

	/**
	 * @param nestedSemanticActionList
	 *          the nestedSemanticActionList to set
	 */
	public void setNestedSemanticActionList(ArrayList<SemanticAction> nestedSemanticActionList)
	{
		this.nestedSemanticActionList = nestedSemanticActionList;
	}

	@Override
	public String getActionName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void setNestedActionState(String name, Object value)
	{
	  if (nestedSemanticActionList != null)
  		for (SemanticAction action : nestedSemanticActionList)
  		{
  			SemanticActionHandler handler = getSemanticActionHandler();
  			action.setSemanticActionHandler(handler);
  			handler.setActionState(action, name, value);
  			action.setNestedActionState(name, value);
  		}
	}
	
}
