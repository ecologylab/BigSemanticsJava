package ecologylab.semantics.actions;

import java.util.ArrayList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionStandardMethods;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
public @xml_tag("create_concept_outlink") class CreateConceptOutlinkSemanticAction extends SemanticAction implements SemanticActionStandardMethods
{
	@Override
	public String getActionName()
	{
		return "create_concept_outlink";
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}
	
	public void handle(Object object, String surface, String concept, ParsedURL link)
	{
		System.out.format("\n--- surface:%s, concept:%s, link:%s ---\n", surface, concept, link.toString());
	}
}
