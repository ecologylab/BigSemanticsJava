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
	@simpl_classes(
	{ FlagCheck.class, OrFlagCheck.class, AndFlagCheck.class, NotFlagCheck.class })
	@simpl_nowrap 
	private ArrayList<FlagCheckBase> flagCheck;
		
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

	/**
	 * @return the flagCheck
	 */
	public final ArrayList<FlagCheckBase> getFlagChecks()
	{
		return flagCheck;
	}

}
