/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.semantics.old.InfoCollector;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.simpl_inherit;

/**
 * THE IF statement
 * 
 * @author amathur
 * 
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.IF)
class IfSemanticAction 
extends	NestedSemanticAction
{

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.IF;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
