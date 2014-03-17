/**
 * 
 */
package ecologylab.bigsemantics.actions;

import java.util.ArrayList;

import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.collections.Scope;
import ecologylab.generic.Continuation;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scope;

/**
 * @author andruid
 *
 */
@simpl_inherit
public abstract class
ContinuableSemanticAction extends SemanticAction
implements Continuation<DocumentClosure>
{
	@simpl_collection
	@simpl_scope(SemanticActionTranslationScope.SEMANTIC_ACTION_TRANSLATION_SCOPE)
	protected ArrayList<SemanticAction>								continuation;


	/**
	 * 
	 */
	public ContinuableSemanticAction()
	{
		super();
	}

	@Override
	public void callback(DocumentClosure documentClosure)
	{
		SemanticActionHandler originalHandler	= this.getSemanticActionHandler();
		
		Scope<Object> originalVariableMap	= originalHandler.getSemanticActionVariableMap();
		
		Scope<Object> newVariableMap = new Scope<Object>(originalVariableMap);
		SemanticActionHandler continuationHandler	= 
			new SemanticActionHandler(documentParser.getSemanticsScope(), documentParser,
					newVariableMap);
		
		newVariableMap.put(SemanticsConstants.DOCUMENT_CALLER, documentParser.getDocument());
		
		//TODO -- how to make old and new Document available
		continuationHandler.takeSemanticActions(originalHandler.metaMetadata, documentClosure.getDocument(), continuation);
	}

	
}
