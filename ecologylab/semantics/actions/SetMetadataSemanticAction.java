/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 *
 */
@xml_inherit
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

}