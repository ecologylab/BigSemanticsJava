package ecologylab.semantics.actions;

import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.TextClipping;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * This action needs to be implemented by the client.
 * 
 * @author quyin
 */
@simpl_inherit
public @simpl_tag(SemanticActionStandardMethods.CREATE_AND_VISUALIZE_TEXT_SURROGATE)
class CreateAndVisualizeTextSurrogateSemanticAction extends SemanticAction implements
		SemanticActionStandardMethods
{

	@Override
	public String getActionName()
	{
		return CREATE_AND_VISUALIZE_TEXT_SURROGATE;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		boolean isSemanticText = getArgumentBoolean(SemanticActionNamedArguments.SEMANTIC_TEXT, false);
		String context = (String) getArgumentObject(SemanticActionNamedArguments.TEXT);
		// TODO use html context -- need methods to strip tags to set regular context from it.
		String htmlContext = (String) getArgumentObject(SemanticActionNamedArguments.HTML_CONTEXT);
		if (context != null)
		{
			Document sourceDocument = resolveSourceDocument();
			TextClipping textClipping = new TextClipping(sessionScope.getMetaMetadataRepository().getMMByName(sessionScope.TEXT_TAG));
			textClipping.setText(context);
			textClipping.setSourceDoc(sourceDocument);
			sourceDocument.addClipping(textClipping);
		}
		return null;
	}
}
