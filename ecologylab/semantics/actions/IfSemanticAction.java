/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;

import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

/**
 * THE IF statement
 * @author amathur
 *
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.IF)
class IfSemanticAction  extends NestedSemanticAction implements SemanticActionStandardMethods
{
	
	@simpl_collection
	@simpl_scope(ConditionTranslationScope.CONDITION_SCOPE)
	@simpl_nowrap 
	private ArrayList<Condition> checks;
		
	@Override
	public String getActionName()
	{
		return IF;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	public final ArrayList<Condition> getChecks()
	{
		return checks;
	}

}
