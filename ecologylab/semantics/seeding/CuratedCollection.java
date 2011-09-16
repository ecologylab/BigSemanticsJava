package ecologylab.semantics.seeding;

import java.util.ArrayList;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_scope;

public class CuratedCollection extends ElementState
{
	@simpl_scalar protected String	 		name;
	
	@simpl_collection("keyword")
	ArrayList<String>										keywordSet;
	
//	@simpl_scalar protected ImageState	icon;
	
	@simpl_collection
	@simpl_nowrap
	@simpl_scope(BaseSeedTranslations.TSCOPE_NAME)
	ArrayList<SeedSet>									arrayList;
	
	public CuratedCollection()
	{
		super();
	}

}
