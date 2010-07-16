package ecologylab.semantics.metametadata.example.googleImage;

import java.util.Map;

import ecologylab.semantics.actions.NestedSemanticAction;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionStandardMethods;

public class googleImageSemanticAction<sa extends SemanticAction> extends NestedSemanticAction<sa> implements SemanticActionStandardMethods  
{

	@Override
	public String getActionName()
	{
		return "save_google_image_metadata";
	}

	@Override
	public void handleError()
	{

	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
//		if(obj instanceof GoogleImage)
//			GoogleImage.metadataCollected.add((GoogleImage)obj)
			
		return null; 
	}
}
