package ecologylab.semantics.metametadata.example.googleImage;

import java.util.Map;

import ecologylab.semantics.actions.NestedSemanticAction;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionStandardMethods;
import ecologylab.semantics.generated.library.GoogleImage;

public class googleImageSemanticAction<sa extends SemanticAction> extends NestedSemanticAction<sa> implements SemanticActionStandardMethods  
{

	@Override
	public String getActionName()
	{
		return "save_metadata";
	}

	@Override
	public void handleError()
	{

	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		if(obj instanceof GoogleImage){
			googleImageDataCollector.metadataCollected.add((GoogleImage)obj);
			
		}	
		return null; 
	}
}
