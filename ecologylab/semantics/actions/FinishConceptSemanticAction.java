package ecologylab.semantics.actions;

import java.util.Map;

import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionStandardMethods;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
@xml_tag("finish_concept")
public class FinishConceptSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{
	@Override
	public String getActionName()
	{
		return "finish_concept";
	}

	@Override
	public void handleError()
	{
	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		ConceptPool.get().endNewConcept();
		return null;
	}
}
