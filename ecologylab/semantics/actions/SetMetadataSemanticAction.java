/**
 * 
 */
package ecologylab.semantics.actions;

import java.util.Map;

import ecologylab.xml.simpl_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 *
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.SET_METADATA) class SetMetadataSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return SET_METADATA;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		// TODO Auto-generated method stub
		System.err.format("The set_metadata semantic action is called! but it is no longer used.");
		return null;
	}

}
