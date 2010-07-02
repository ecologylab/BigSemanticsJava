package ecologylab.semantics.actions;

import java.util.Map;

import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

@xml_inherit
@xml_tag("search")
public class SearchSemanticAction<SA extends SemanticAction> extends NestedSemanticAction<SA>
{

	private static final String	ARG_QUERY	= "query";
	
	@xml_attribute
	private String engine;
	
	@Override
	public String getActionName()
	{
		return "search";
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object handle(Object obj, Map<String, Object> args)
	{
		String query = (String) args.get(ARG_QUERY);
		
		InfoCollector ic = getInfoCollector();
		SeedSet seedSet = ic.getSeedSet();
		SearchState search = new SearchState(query, engine);
		seedSet.add(search);
		seedSet.performSeeding(ic.sessionScope());
		return null;
	}
}
