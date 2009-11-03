/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.ArrayList;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_collection;
import ecologylab.xml.ElementState.xml_nowrap;
import ecologylab.xml.ElementState.xml_tag;
import ecologylab.xml.types.element.ArrayListState;

/**
 * THE IF statement
 * @author amathur
 *
 */
@xml_inherit
public @xml_tag(SemanticActionStandardMethods.IF)
class IfSemanticAction  extends NestedSemanticAction implements SemanticActionStandardMethods
{
	@xml_nowrap 
	@xml_collection("flag_check")
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
