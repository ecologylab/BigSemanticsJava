/**
 * 
 */
package ecologylab.semantics.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.simpl_inherit;

/**
 * transform_location semantic action, with set_param, strip_param semantic actions inside it, 
 * for managing variability in Document location ParsedURL arguments.
 * 
 * Operations include set_param & strip_param.
 * @author andruid
 *
 */
@simpl_inherit
@xml_tag(SemanticActionStandardMethods.TRANSFORM_LOCATION)
public class TransformLocation extends SemanticAction
{
	@simpl_classes({SetParam.class})
	@simpl_nowrap
	ArrayList<ParamOp>	paramOps;

	/**
	 * 
	 */
	public TransformLocation()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.TRANSFORM_LOCATION;
	}

	@Override
	public void handleError()
	{

	}

/**
 * Extract location ParsedURL parameters into a HashMap.
 * Let ParamOps operate on this Map.
 * Derive a new ParsedURL using the base of the original location's ParsedURL and the transformed parameter map.
 * If the new ParsedURL is different than the old one, make the old one an additional location for this,
 * and add the transformed ParsedURL to the DocumentLocationMap.
 */
	@Override
	public Object perform(Object obj) throws IOException
	{
		Document document								= documentParser.getDocument();
		if (paramOps != null && paramOps.size() > 0)
		{
			final ParsedURL origLocation 					= document.getLocation();
			HashMap<String, String>	parametersMap	= origLocation.extractParams();
			if (parametersMap == null)
				parametersMap												= new HashMap<String, String>(paramOps.size());
			for (ParamOp paramOp: paramOps)
			{
				paramOp.transformParams(parametersMap);
			}
			ParsedURL transformedLocation		= origLocation.updateParams(parametersMap);
			if (origLocation != transformedLocation)
			{
				document.addAdditionalLocation(origLocation);
				document.setLocation(transformedLocation);
				documentParser.getSemanticsScope().getGlobalCollection().addMapping(transformedLocation, document);
			}
		}
		return null;
	}

}
