package ecologylab.semantics.metametadata.example.bingImage;

import java.util.Map;

import ecologylab.semantics.actions.NestedSemanticAction;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.metametadata.example.bingImage.generated.BingImageType;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag("save_image_metadata")
public class bingImageSemanticAction extends SemanticAction  
{

	@Override
	public String getActionName()
	{
		return "save_image_metadata";
	}

	@Override
	public void handleError()
	{

	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		System.out.println(this.getClass().getName() + " [handle] : " + obj);
//		if(obj instanceof BingImageType){
			System.out.println("[bing image type]");
			bingImageDataCollector.metadataCollected.add((BingImageType)obj);
			
//		}else{
//			System.out.println("[what else]");
//		}
		
		return null; 
	}
}
