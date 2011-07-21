package ecologylab.semantics.seeding;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import ecologylab.collections.Scope;
import ecologylab.semantics.collecting.Seeding;
import ecologylab.semantics.collecting.SemanticsGlobalScope;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.namesandnums.SemanticsSessionObjectNames;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.simpl_inherit;

/**
 * A collection of seeds that will be performed by the agent, or elsewhere, by composition space
 * services. These are directives from the user via the startup enviornment.
 * 
 * @author andruid
 */
@simpl_inherit
public class SeedSet<S extends Seed> extends ElementState
		implements SemanticsSessionObjectNames, Iterable<S>
{
	static TranslationScope	ts								= BaseSeedTranslations.get();

	@simpl_scalar
	protected boolean				dontPlayOnStart;

	@simpl_scalar
	protected String				id;

	@simpl_scalar
	protected String				category;

	@simpl_scalar
	protected String				description;

	@simpl_collection
	@simpl_scope(BaseSeedTranslations.TSCOPE_NAME)
	@simpl_nowrap
	protected ArrayList<S>	seeds;

	/**
	 * when this is set to true, some thread is now iterating this seed set's seeds field, thus you
	 * can't modify it right now. instead you use pendingSeedsToAdd.
	 */
	private boolean					duringIteration		= false;

	/**
	 * seeds in this set will be added to seeds in the next seeding phase (and also removed from
	 * here).
	 */
	private ArrayList<S>		pendingSeedsToAdd	= new ArrayList<S>();

	public SeedSet()
	{

	}

	// public static SeedSet getFromXML(String seedSetString)
	// {
	// try
	// {
	// return (SeedSet) translateFromXMLCharSequence(seedSetString, CFServicesTranslations.get());
	// } catch (XMLTranslationException e)
	// {
	// println("ERROR parsing seedSetString.");
	// e.printStackTrace();
	// return null;
	// }
	// }

	/**
	 * Number of SearchState seeds specified in this.
	 */
	private int			numSearches	= 0;

	/**
	 * Coordinate downloading of search results for seeding searches.
	 */
	SeedDistributor	resultDistributer;

	/**
	 * Some of seeds such as YahooBuzzType have a starting seed(a buzz page) from an initial SeedSet
	 * and create a new SeedSet from the buzz page. Reference to the parent SeedSet from a child
	 * SeedSet.
	 */
	SeedSet					parentSeedSet;

	public SeedDistributor seedDistributer(SemanticsGlobalScope infoCollector)
	{
		SeedDistributor result = resultDistributer;

		if (result == null)
		{
			if (parentSeedSet != null) // recurse to get from parent
			{
				result = parentSeedSet.seedDistributer(infoCollector);
				// result.moreSearches(numSearches);
			}
			else
				result = new SeedDistributor(infoCollector);
			this.resultDistributer = result;
		}

		return result;
	}

	/**
	 * In case for replace seed with the curated-seeds, SeedSet is reused. So, old resultDistributer
	 * is used without getting reset. Maybe there is better place to reset ResultDistributer -- eunyee
	 * 
	 * @return
	 */
	public void resetResultDistributer()
	{

		if (resultDistributer != null)
			resultDistributer.reset();
	}

	/**
	 * Set the reference of the parent SeedSet.
	 * 
	 * @param seedDistributer
	 */
	public void setParentSeedSet(SeedSet seedSet)
	{
		this.parentSeedSet = seedSet;
	}

	/**
	 * Update parameters for next result set. Set time stamp to now. Perform the resulting search by
	 * using the engine as a key .
	 * 
	 * @param semanticsSessionScope
	 */
	public void performNextSeeding(Scope scope)
	{
		// resultDistributer = null;
		resetResultDistributer();

		performSeeding(scope, true);
	}

	public void performSeeding(Scope scope)
	{
		performSeeding(scope, false);
	}

	int	startingResultNum;

	public int getStartingResultNum()
	{
		return startingResultNum;
	}

	/**
	 * Bring the seeds into the agent or directly into the compostion.
	 * 
	 * @param scope
	 *          Context passed between services calls.
	 */
	public void performSeeding(Scope scope, boolean nextSearch)
	{
		if (pendingSeedsToAdd.size() > 0)
		{
			// be careful of possible deadlock here
			synchronized (pendingSeedsToAdd)
			{
				synchronized (seeds)
				{
					for (S seed : pendingSeedsToAdd)
						seeds.add(seed);
				}
				pendingSeedsToAdd.clear();
			}
		}

		if (size() == 0)
			return;

		SemanticsSessionScope infoCollector = (SemanticsSessionScope) scope.get(INFO_COLLECTOR);
		
		Seeding seeding	= infoCollector.getSeeding();

		seeding.trackFirstSeedSet(this);
		seeding.setPlayOnStart(true);

		seeding.beginSeeding();

		numSearches = 0; // reset each time performSeeding is called

		// search bookkeeping for each seed
		duringIteration = true;
		for (Seed seed : seeds)
		{
			if (nextSearch)
			{
				// seed.seedDistributer = null; // it seems that we can reuse the previous seed distributor
				int thisStartingResultNum = seed.nextResultSet();
				if (thisStartingResultNum > startingResultNum)
					startingResultNum = thisStartingResultNum;
			}
			else if (seed.initializeSeedingSteps(this, numSearches))
				numSearches++;
		}
		duringIteration = false;

		// We need the same two for loops (above and below), because
		// it requires to have total searchNum before it starts performSeeding.
		// Do not try to aggregate these two for loops unless you have better structure.
		boolean aSeedingIsPerformed = false;
		boolean shouldEndSeeding = true;
		duringIteration = true;
		for (Seed seed : seeds)
		{
			seed.fixNumResults();

			if (seed.isActive()) // false if inactive
			{
				debug("perform seeding: " + seed);

				if (shouldNotEndSeeding(seed))
					shouldEndSeeding = false;

				seed.performSeedingSteps(infoCollector);
				aSeedingIsPerformed = true;

				SeedPeer seedPeer = seed.getSeedPeer();
				if (seedPeer != null)
					seedPeer.notifyInterface(scope, SEARCH_DASH_BOARD);

				seed.setActive(false); // does not affect SearchState or Feed
			}
		}
		duringIteration = false;

		if (aSeedingIsPerformed && shouldEndSeeding)
		{
			seeding.endSeeding();
			seedDistributer(infoCollector).stop();
		}
	}

	/**
	 * 
	 * @param seed
	 * @return true if we should not call endSeeding() immediately because this seed has outsourced
	 *         its seeding steps (e.g. through a SeedDistributor). otherwise false.
	 */
	private boolean shouldNotEndSeeding(Seed seed)
	{
		if (seed instanceof SearchState)
			return true;
		if (seed instanceof Feed)
			return true;
		return false;
	}

	public String toString()
	{
		return "SeedSet[" + size() + "]";
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	private int handleMoreSeedsDialogue(SemanticsSessionScope infoCollector)
	{
		String seedText = "";
		for (int i = 0; i < size(); i++)
			seedText += "        " + ((Seed) get(i)).valueString() + "\n";
		if (!"".equals(seedText))
			seedText += "\n";
		String optionsMessage = "combinFormation has received a new set of seeds:\n" +
				seedText +
				"\t- Do you want to mix these new seeds with the already existing seeds?\n" +
				"\t- Do you want to replace the current seeds with these new seeds?\n" +
				"\t- Do you want to ignore these new seeds?";
		
		return infoCollector.showOptionsDialog(optionsMessage, "Warning", SeedCf.MULTIPLE_REQUESTS_DIALOG_OPTIONS, 0);
	}

	public void handleMoreSeeds(Scope clientConnectionScope, int selectedOption)
	{
		if (selectedOption == SeedCf.MULTIPLE_REQUESTS_ASK_USER)
		// if (true)
		{
			if (size() == 0)
			{
				selectedOption = SeedCf.MULTIPLE_REQUESTS_IGNORE;
				debug("Received seeding request with 0 seeds. Ignoring!");
			}
			else
			{
				SemanticsSessionScope infoCollector = (SemanticsSessionScope) clientConnectionScope.get(INFO_COLLECTOR);
				
				selectedOption = handleMoreSeedsDialogue(infoCollector);
			}
		}
		switch (selectedOption)
		{
		case SeedCf.MULTIPLE_REQUESTS_MIX:
			performSeeding(clientConnectionScope);
			break;
		case SeedCf.MULTIPLE_REQUESTS_REPLACE:
			debug("handleMoreSeeds(REPLACE) " + clientConnectionScope.dump());
			SemanticsSessionScope infoCollector = (SemanticsSessionScope) clientConnectionScope.get(INFO_COLLECTOR);
			infoCollector.clear();
			performSeeding(clientConnectionScope);
			break;
		case SeedCf.MULTIPLE_REQUESTS_IGNORE:
		case JOptionPane.CLOSED_OPTION:
			debug("Ignoring multiple request.");
			break;
		default:
		}
	}

	public void add(S seed, SemanticsGlobalScope infoCollector)
	{
		if (seed != null)
			if (seeds == null)
				seeds = new ArrayList<S>();
		
		if (duringIteration)
		{
			synchronized(pendingSeedsToAdd)
			{
				pendingSeedsToAdd.add(seed);
			}
		}
		else
		{
			synchronized(seeds)
			{
				seeds.add(seed);
			}
		}
		seed.setSeedSet(this);

		Seeding seeding	= infoCollector.getSeeding();

		// Test for hetero/homogeneity
		Iterator iterator = iterator();
		while (iterator.hasNext())
		{
			Seed s = (Seed) iterator.next();
			if (size() == 1)
			{
				seeding.setHeterogeneousSearchScenario(!s.isHomogenousSeed());
				break;
			}
			else
			{
				// More than one seed.
				// Even if one isn't homogeneous, scenario is hetero
				if (!s.isHomogenousSeed())
				{
					seeding.setHeterogeneousSearchScenario(true);
					break;
				}
			}
		}
	}

	public void clear()
	{
		if (seeds != null)
			seeds.clear();
	}

	public int size()
	{
		return (seeds != null) ? seeds.size() : 0;
	}

	public Iterator<S> iterator()
	{
		return seeds.iterator();
	}

	public S get(int i)
	{
		return seeds != null ? (i >= 0 && i < seeds.size() ? seeds.get(i) : null) : null;
	}

	public int indexOf(S that)
	{
		return seeds != null ? seeds.indexOf(that) : -1;
	}

	public boolean isEmpty()
	{
		return seeds == null || seeds.isEmpty();
	}

}