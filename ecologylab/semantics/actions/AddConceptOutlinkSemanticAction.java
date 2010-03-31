package ecologylab.semantics.actions;

import java.util.Map;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionStandardMethods;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
@xml_tag("add_concept_outlink")
public class AddConceptOutlinkSemanticAction extends SemanticAction implements
		SemanticActionStandardMethods
{
	@Override
	public String getActionName()
	{
		return "add_concept_outlink";
	}

	@Override
	public void handleError()
	{
	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		String surface = (String) args.get("surface");
		String targetConcept = (String) args.get("target_concept");
		
		ConceptPool.get().addOutlink(surface, targetConcept);
		return null;
	}
}
