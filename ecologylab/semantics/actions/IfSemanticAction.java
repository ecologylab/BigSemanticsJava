/**
 * 
 */
package ecologylab.semantics.actions;

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
	@xml_tag("flag_checks") @xml_collection("flag_checks")
	private ArrayListState<FlagCheck> flagCheck;
		
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
	public final ArrayListState<FlagCheck> getFlagCheck()
	{
		return flagCheck;
	}

	/**
	 * @param flagCheck the flagCheck to set
	 */
	public final void setFlagCheck(ArrayListState<FlagCheck> flagCheck)
	{
		this.flagCheck = flagCheck;
	}

	

}
