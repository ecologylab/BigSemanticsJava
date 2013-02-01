/**
 * 
 */
package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_classes;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Semantics that transforms locations, with set_param, strip_param semantic actions inside it, 
 * for managing variability in Document location ParsedURL arguments.
 * 
 * Operations include set_param & strip_param.
 * 
 * @author andruid
 */
public class FilterLocation extends Debug
{
  
	@simpl_classes({SetParam.class, StripParam.class})
	@simpl_nowrap
	@simpl_collection
	ArrayList<ParamOp>	paramOps;

	@simpl_nowrap
	@simpl_collection("alternative_host")
	ArrayList<String>	  alternativeHosts;

	@simpl_composite
	Regex								regex;
	
	@simpl_scalar
	String							stripPrefix;
	
	/**
	 * 
	 */
	public FilterLocation()
	{
		// TODO Auto-generated constructor stub
	}

  public ArrayList<ParamOp> getParamOps()
  {
    return paramOps;
  }

  public void setParamOps(ArrayList<ParamOp> paramOps)
  {
    this.paramOps = paramOps;
  }

  public ArrayList<String> getAlternativeHosts()
  {
    return alternativeHosts;
  }

  public void setAlternativeHosts(ArrayList<String> alternativeHosts)
  {
    this.alternativeHosts = alternativeHosts;
  }

  public Regex getRegex()
  {
    return regex;
  }

  public void setRegex(Regex regex)
  {
    this.regex = regex;
  }

  public String getStripPrefix()
  {
    return stripPrefix;
  }

  public void setStripPrefix(String stripPrefix)
  {
    this.stripPrefix = stripPrefix;
  }

  /**
   * Extract location ParsedURL parameters into a HashMap. Let ParamOps operate on this Map. Derive
   * a new ParsedURL using the base of the original location's ParsedURL and the transformed
   * parameter map. Put alternative locations (locations with alternative domains in an outgoing
   * collection.
   * 
   * @param origLocation
   *          The original location.
   * @param alternativeLocations
   *          Locations with alternative domains.
   * @return The filtered (normalized) location.
   */
	public ParsedURL filter(ParsedURL origLocation, List<ParsedURL> alternativeLocations)
	{
		ParsedURL filteredLocation      = origLocation;
		
		if (origLocation.isFile())
		{
			warning("Not doing <filter_location> because this is a file: " + origLocation);
		}
		else
		{
  		if (paramOps != null && paramOps.size() > 0)
  		{
  			HashMap<String, String>	parametersMap	= filteredLocation.extractParams(true);
  			if (parametersMap == null)
  				parametersMap												= new HashMap<String, String>(paramOps.size());
  			for (ParamOp paramOp: paramOps)
  			{
  				paramOp.transformParams(parametersMap);
  			}
  			filteredLocation = filteredLocation.updateParams(parametersMap);
  		}
  		
  		if (regex != null)
  		{
  			filteredLocation	= regex.perform(filteredLocation);
  		}
  		
  		if (stripPrefix != null)
  		{
  			String locationString	= filteredLocation.toString();
  			int index	= locationString.indexOf(stripPrefix);
  			if (index > 6)
  			{
  				String newLocationString	= locationString.substring(0, index);
  				filteredLocation			    = ParsedURL.getAbsolute(newLocationString);
  			}
  		}
  		
  		if (alternativeHosts != null)
  		{
  			final String origHost	= filteredLocation.host();
  			for (String alternativeHost: alternativeHosts)
  			{
  				if (!origHost.equals(alternativeHost))
  				{
  					ParsedURL newLocation	= filteredLocation.changeHost(alternativeHost);
  					if (alternativeLocations != null)
    					alternativeLocations.add(newLocation);
  				}
  			}
  		}
		}
		
		return filteredLocation;
	}
	
	/**
	 * Filter the input Purl if needed, i.e. specified in the meta-metadata repository. It will first
	 * match the input Purl with a meta-metadata, and then use filtering rules for that meta-metadata
	 * to do filtering.
	 * 
	 * @param purl
	 * @param alternatives
	 * @param semanticsScope
	 * @return
	 */
	public static ParsedURL filterIfNeeded(ParsedURL purl,
	                                       List<ParsedURL> alternatives,
	                                       SemanticsGlobalScope semanticsScope)
	{
	  MetaMetadata mm = semanticsScope.getMetaMetadataRepository().getDocumentMM(purl);
    FilterLocation filter = mm == null ? null : mm.getFilterLocation();
	  if (filter != null)
	  {
	    ParsedURL filteredPurl = filter.filter(purl, alternatives);
	    return filteredPurl;
	  }
	  return purl;
	}

}
