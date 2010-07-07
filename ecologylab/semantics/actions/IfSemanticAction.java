/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;

import ecologylab.xml.simpl_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * THE IF statement
 * @author amathur
 *
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.IF)
class IfSemanticAction  extends NestedSemanticAction implements SemanticActionStandardMethods
{
	@simpl_nowrap 
	@simpl_collection("flag_check")
	private ArrayList<FlagCheck> flagCheck;
		
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
	public final ArrayList<FlagCheck> getFlagChecks()
	{
		return flagCheck;
	}

}
