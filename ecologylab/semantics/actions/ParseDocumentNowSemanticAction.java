/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;


/**
 * @author amathur
 *
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.PARSE_DOCUMENT_NOW)
class ParseDocumentNowSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return PARSE_DOCUMENT_NOW;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

}
