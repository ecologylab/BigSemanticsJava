package ecologylab.bigsemantics.actions;

import ecologylab.bigsemantics.seeding.SearchState;
import ecologylab.bigsemantics.seeding.SeedSet;
import ecologylab.generic.StringTools;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

@simpl_inherit
@simpl_tag(SemanticActionStandardMethods.SEARCH)
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
	  if (sessionScope.isService())
	  {
	    return null;
	  }

		String query = (String) getArgumentObject(ARG_QUERY);
		if (StringTools.isNullOrEmpty(query))
			return null;

		SearchState search = new SearchState(query, engine);
		search.initialize(sessionScope);
		SeedSet seedSet = new SeedSet();
		seedSet.setParentSeedSet(sessionScope.getSeeding().getSeedSet());
		seedSet.add(search, sessionScope);
		seedSet.performSeeding(sessionScope, true);
		return null;
	}
}
