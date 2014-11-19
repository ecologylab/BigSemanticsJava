/**
 * 
 */
package ecologylab.bigsemantics.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ecologylab.bigsemantics.collecting.LocalDocumentCollections;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.FilterLocation;
import ecologylab.bigsemantics.metametadata.OverrideParams;
import ecologylab.bigsemantics.metametadata.ParamOp;
import ecologylab.bigsemantics.metametadata.Regex;
import ecologylab.bigsemantics.metametadata.SetParam;
import ecologylab.bigsemantics.metametadata.StripParam;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_classes;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
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
public class FilterLocationAction extends SemanticAction
{
  
  // hacky way to make this work for both inside metadata and in semantic actions :(
  // we should really make simpl_nowrap work for composte correctly and use it instead of
  // cloning fields here!
  
  @simpl_composite
  private OverrideParams overrideParams;
  
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
	
//	@simpl_nowrap
//	@simpl_composte
//	FilterLocation      filter;
 
	/**
	 * 
	 */
	public FilterLocationAction()
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
	
	private FilterLocation getFilter()
	{
	  FilterLocation filter = new FilterLocation();
	  filter.setOverrideParams(overrideParams);
	  filter.setParamOps(paramOps);
	  filter.setAlternativeHosts(alternativeHosts);
	  filter.setRegex(regex);
	  filter.setStripPrefix(stripPrefix);
	  return filter;
	}

  /**
   * Extract location ParsedURL parameters into a HashMap. Let ParamOps operate on this Map. Derive
   * a new ParsedURL using the base of the original location's ParsedURL and the transformed
   * parameter map. If the new ParsedURL is different than the old one, make the old one an
   * additional location for this, and add the transformed ParsedURL to the DocumentLocationMap.
   * <p/>
   * If the location changes, then reconnect to the new one.
   */
	@Override
	public Object perform(Object obj) throws IOException
	{
		boolean usingThisDoc						= true; // if we are changing this document's location, or its child field's location
		
		Document document								= documentParser.getDocument();
		if (getObject() != null)
		{
			Object o = getSemanticActionHandler().getSemanticActionVariableMap().get(getObject());
			if (o != null && o instanceof Document)
			{
				document = (Document) o;
				usingThisDoc = false;
			}
		}
		
		final LocalDocumentCollections globalCollection = documentParser.getSemanticsScope().getLocalDocumentCollection();
		
		final ParsedURL origLocation 		= document.getLocation();
		if (origLocation.isFile())
		{
			warning("Not doing <filter_location> because this is a file: " + origLocation);
			return null;
		}
		
		List<ParsedURL> alternativeLocations = new ArrayList<ParsedURL>();
    ParsedURL filteredLocation = getFilter().filter(origLocation, alternativeLocations);
    
    boolean locationChanged = !origLocation.equals(filteredLocation);
    if (locationChanged)
    {
      // location changed!
      document.changeLocation(filteredLocation);
      document.addAdditionalLocation(origLocation);
    }
    
    for (ParsedURL alternativeLocation : alternativeLocations)
    {
      globalCollection.addMapping(alternativeLocation, document);
      document.addAdditionalLocation(alternativeLocation);
    }
	
		if (locationChanged && usingThisDoc) // if we are just changing the location of a field, we don't have to reconnect.
			documentParser.reConnect();		// changed the location, so we better connect again!
		
		return null;
	}

	static void changeLocation(final LocalDocumentCollections globalCollection, Document document,
			final ParsedURL origLocation, ParsedURL transformedLocation)
	{
		document.addAdditionalLocation(origLocation);
		document.setLocation(transformedLocation);
		globalCollection.addMapping(transformedLocation, document);
	}

}
