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
public @xml_tag(SemanticActionStandardMethods.CREATE_AND_VISUALIZE_IMG_SURROGATE) class  CreateAndVisualizeImgSurrogateSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return CREATE_AND_VISUALIZE_IMG_SURROGATE;
	}

}
