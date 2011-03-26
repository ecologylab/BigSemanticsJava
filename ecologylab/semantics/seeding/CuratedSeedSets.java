package ecologylab.semantics.seeding;

import java.io.File;
import java.util.ArrayList;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.io.Assets;
import ecologylab.semantics.namesandnums.CFPrefNames;
import ecologylab.semantics.namesandnums.SemanticsAssetVersions;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.simpl_inherit;

/**
 * A collection + registry of SeedSets.
 *
 * @author andruid
 */
public @simpl_inherit class CuratedSeedSets extends ElementState
implements CFPrefNames
{
	@simpl_collection
	@simpl_nowrap
	@simpl_scope(BaseSeedTranslations.TSCOPE_NAME)
	ArrayList<SeedSet>									arrayList;
	
	static CuratedSeedSets	singleton;
	
	static TranslationScope TSCOPE	= TranslationScope.get("curated_seed_sets", BaseSeedTranslations.get(), CuratedSeedSets.class);
	public CuratedSeedSets()
	{
		super();
	}
	
	//TODO -- don't use getElementStateById; instead use @simpl_map
	public static SeedSet lookup(String id)
	{
		if (singleton == null)
			init();
		return (SeedSet) singleton.getElementStateById(id);
	}

	private static void init()
	{
		File seedingsFile	= Assets.getAsset(SemanticsAssetVersions.SEMANTICS_ASSETS_ROOT, CFPrefNames.CURATED + "/curated_seed_sets.xml");
		if (!ApplicationEnvironment.runningInEclipse())
			Assets.updateAssetsXml("CuratedSeedSets.init()");
		try
		{
			CuratedSeedSets cs		= (CuratedSeedSets) TSCOPE.deserialize(seedingsFile);
			singleton				= cs;
		} catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}
	
	static CuratedSeedSets all()
	{
		if (singleton == null)
			init();
		return singleton;
	}
}
