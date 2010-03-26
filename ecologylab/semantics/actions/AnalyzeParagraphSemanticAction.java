package ecologylab.semantics.actions;

import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionStandardMethods;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
@xml_tag("analyze_paragraph")
public class AnalyzeParagraphSemanticAction extends SemanticAction implements
		SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	public void handle(Object object, String paragraphText)
	{
		System.out.println(paragraphText);
	}
}
