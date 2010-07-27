/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.serialization.Hint;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;


/**
 * @author amathur
 *
 */
@simpl_inherit
public @xml_tag(SemanticActionStandardMethods.PARSE_DOCUMENT)
class ParseDocumentSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@simpl_scalar
	@simpl_hints(Hint.XML_ATTRIBUTE)
	private boolean now = false;
	
	public boolean isNow()
	{
		return now;
	}
	
	@Override
	public String getActionName()
	{
		return PARSE_DOCUMENT;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

}
