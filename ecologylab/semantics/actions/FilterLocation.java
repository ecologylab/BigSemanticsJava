/**
 * 
 */
package ecologylab.semantics.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.TNGGlobalCollections;
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
@xml_tag(SemanticActionStandardMethods.FILTER_LOCATION)
public class FilterLocation extends SemanticAction
{
	@simpl_classes({SetParam.class, StripParam.class})
	@simpl_nowrap
	@simpl_collection
	ArrayList<ParamOp>	paramOps;

	@simpl_nowrap
	@simpl_collection("alternative_host")
	ArrayList<String>	alternativeHosts;

	
	/**
	 * 
	 */
	public FilterLocation()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.FILTER_LOCATION;
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
		final ParsedURL origLocation 		= document.getLocation();
		final TNGGlobalCollections globalCollection = documentParser.getSemanticsScope().getGlobalCollection();
		if (paramOps != null && paramOps.size() > 0)
		{
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
				globalCollection.addMapping(transformedLocation, document);
				
				documentParser.reConnect();		// changed the location, so we better connect again!
			}
		}
		if (alternativeHosts != null)
		{
			final String origHost	= origLocation.host();
			for (String alternativeHost: alternativeHosts)
			{
				if (!origHost.equals(alternativeHost))
				{
					ParsedURL newLocation	= origLocation.changeHost(alternativeHost);
					document.addAdditionalLocation(newLocation);
					globalCollection.addMapping(newLocation, document);
				}
			}
		}
		return null;
	}

}
