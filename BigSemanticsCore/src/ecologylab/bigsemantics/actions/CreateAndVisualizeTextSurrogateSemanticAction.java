package ecologylab.bigsemantics.actions;

import ecologylab.bigsemantics.metadata.builtins.RichDocument;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.TextClipping;
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

	static final int MAX_WORDS_IN_GIST = 8;
	private String createGist(String text)
	{
		String[] words = text.split(" ");
		String returnString = "";
		int wordCount = 0;
		for(String word : words)
		{
			if(wordCount > 0)
			returnString += " ";
			returnString += word;
			wordCount++;
			if(wordCount >= MAX_WORDS_IN_GIST)
				break;
		}
		return returnString;
	}
	
	@Override
	public Object perform(Object obj)
	{
		debug("Adding text clipping");
		boolean isSemanticText = getArgumentBoolean(SemanticActionNamedArguments.SEMANTIC_TEXT, false);
		String context = (String) getArgumentObject(SemanticActionNamedArguments.TEXT);
		// TODO use html context -- need methods to strip tags to set regular context from it.
		String htmlContext = (String) getArgumentObject(SemanticActionNamedArguments.HTML_CONTEXT);
		if (context != null)
		{
			Document sourceDocument = resolveSourceDocument();
			//We will do something smarter here later when we have interest vectors.
			TextClipping textClipping = new TextClipping(sessionScope.getMetaMetadataRepository().getMMByName(sessionScope.TEXT_TAG));
		  ///textClipping.setText(createGist(context));
			textClipping.setText(context);
			textClipping.setContext(context);

			textClipping.setSourceDoc(sourceDocument);
			if (sourceDocument instanceof RichDocument)
				((RichDocument) sourceDocument).addClipping(textClipping);
		}
		return null;
	}
}
