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
public @xml_tag(SemanticActionStandardMethods.PROCESS_DOCUMENT)class ProcessDocumentSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return PROCESS_DOCUMENT;
	}

}
