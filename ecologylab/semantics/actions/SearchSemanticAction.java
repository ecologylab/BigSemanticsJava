package ecologylab.semantics.actions;

import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag(SemanticActionStandardMethods.SEARCH)
public class SearchSemanticAction
		extends SemanticAction
{

	protected static final String	ARG_QUERY	= "query";

	@simpl_scalar
	protected String							engine;

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.SEARCH;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		String query = (String) getArgumentObject(ARG_QUERY);
		if (query == null || query.isEmpty())
			return null;

		SearchState search = new SearchState(query, engine);
		search.initialize(infoCollector);
		SeedSet seedSet = new SeedSet();
		seedSet.setParentSeedSet(infoCollector.getSeedSet());
		seedSet.add(search, infoCollector);
		seedSet.performSeeding(infoCollector.sessionScope(), true);
		return null;
	}
}
