package ecologylab.semantics.seeding;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.appframework.types.prefs.PrefInt;

public interface SemanticsPrefs
{

	public static final PrefInt	NUM_SEARCH_RESULTS		= Pref.usePrefInt("num_search_results", 20);

}
