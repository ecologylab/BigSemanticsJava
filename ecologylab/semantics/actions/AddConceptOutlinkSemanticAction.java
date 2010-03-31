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

		String linkS = String.format(
				"[esc:name \"%s\"] esc:linked_by [esc:name \"%s\"]; esc:surface \"%s\" .",
				targetConcept, ConceptPool.get().getCurrent().getName(), surface);
		StringPool.get("inlinks.n3").addLine(linkS);

		String surfaceS = String.format(
				"[esc:words \"%s\"] esc:surface_of [esc:name \"%s\"] .", surface, targetConcept);
		StringPool.get("surfaces.n3").addLine(surfaceS);

		return null;
	}
}
