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
import ecologylab.serialization.annotations.simpl_classes;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * transform_location semantic action, with set_param, strip_param semantic actions inside it, 
 * for managing variability in Document location ParsedURL arguments.
 * 
 * Operations include set_param & strip_param.
 * @author andruid
 *
 */
@simpl_inherit
@simpl_tag(SemanticActionStandardMethods.FILTER_LOCATION)
public class FilterLocation extends SemanticAction
{
	@simpl_classes({SetParam.class, StripParam.class})
	@simpl_nowrap
	@simpl_collection
	ArrayList<ParamOp>	paramOps;

	@simpl_nowrap
	@simpl_collection("alternative_host")
	ArrayList<String>	alternativeHosts;

	@simpl_composite
	Regex								regex;
	
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
 * <p/>
 * If the location changes, then reconnect to the new one.
 */
	@Override
	public Object perform(Object obj) throws IOException
	{
		Document document								= documentParser.getDocument();
		final ParsedURL origLocation 		= document.getLocation();
		if (origLocation.isFile())
		{
			warning("Not doing <filter_location> because this is a file: " + origLocation);
			return null;
		}
		
		final TNGGlobalCollections globalCollection = documentParser.getSemanticsScope().getGlobalCollection();
		boolean locationChanged					= false;
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
				document.changeLocation(transformedLocation);
//				changeLocation(globalCollection, document, origLocation, transformedLocation);
				
				locationChanged			= true;
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
		if (regex != null)
		{
			ParsedURL location	= document.getLocation();
			ParsedURL regexURL	= regex.perform(location);
			document.changeLocation(regexURL);
			locationChanged			= true;
		}
		if (locationChanged)
			documentParser.reConnect();		// changed the location, so we better connect again!
		return null;
	}

	static void changeLocation(final TNGGlobalCollections globalCollection, Document document,
			final ParsedURL origLocation, ParsedURL transformedLocation)
	{
		document.addAdditionalLocation(origLocation);
		document.setLocation(transformedLocation);
		globalCollection.addMapping(transformedLocation, document);
	}

}
