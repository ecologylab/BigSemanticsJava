/**
 * 
 */
package ecologylab.semantics.collecting;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import ecologylab.collections.PrefixCollection;
import ecologylab.collections.PrefixPhrase;
import ecologylab.generic.Debug;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedDistributor;
import ecologylab.semantics.seeding.SeedPeer;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.semantics.seeding.SemanticsPrefs;
import ecologylab.serialization.ElementState;

/**
 * All state related to Seeding.
 * 
 * @author andruid
 */
public class Seeding extends ElementState
implements SemanticsPrefs
{
	private SemanticsSessionScope 	semanticsSessionScope;
	
	private Crawler						crawler;
	/**
	 * 
	 */
	public Seeding(SemanticsSessionScope semanticsSessionScope)
	{
		this.semanticsSessionScope	= semanticsSessionScope;
		this.crawler								= semanticsSessionScope.getCrawler();
	}

	////////////////////////////////////////// seeding stuff ////////////////////////////////////////////////////////////
	/**
	 * Bias for ads: if filter matches a url to an ad (for HTMLPages or MediaElements), multiply bias
	 * by this number. Special values: 0 reject ads (eg, filter matches), altogether
	 */
	protected float															adsBias;

	/**
	 * Hashtable of domains the information space author doesn't want any information elements from.
	 */
	protected HashSet<String>	rejectDomains									= new HashSet<String>();

	/**
	 * A count of seeds whose downloading failed. Used during startup, to determine whether it is time
	 * to start the crawler. We make sure to download all seeds before giving resources to the
	 * crawler. This makes startup fairer; otherwise, the first recorded seeds get too much priority.
	 */
	protected int																badSeeds;
	
	protected boolean														playOnStart;

	/**
	 * Address prefixes derived from original seeding, traversable| seeding specs, and server-side
	 * redirects. Defines the spanning set of basis vectors of the information space, when limit
	 * traversal (stay close) is true.
	 */
	// private Vector<String> traversableURLStrings = new Vector<String>();
	protected PrefixCollection									traversablePrefixes						= new PrefixCollection();

	/**
	 * Untraversable urls defined at seeding time. These supercede traversable specs.
	 */
	// Vector<String> untraversableURLStrings = new Vector<String>();
	protected PrefixCollection									untraversablePrefixes					= new PrefixCollection();

	private SeedSet															seedSet;

	//
	static final Object													SEEDING_STATE_LOCK = new Object();

	protected boolean														duringSeeding;

	public boolean 															heterogeneousSearchScenario = true;
	
	boolean 																		acceptAll;

	public void setHeterogeneousSearchScenario(boolean b)
	{
		this.heterogeneousSearchScenario	= b;
	}

	// ++++++++++++++++++++++++++++++++++++++++ //
	
	/**
	 * Add the directory that this URL references to the traversable set; that is, to the bounding set
	 * of path prefixes that we are willing to download from, given "limit traversal." This is called
	 * automatically, as well as through traversable|; thus it parses to the directory level, removing
	 * any filename portion of the URL.
	 */
	public void traversable(ParsedURL purl)
	{
		traversable(purl, false);
	}
	
	public void traversable(ParsedURL purl, boolean ignoreReject)
	{
		// String uniquePrefix = purl.directoryString();
		// debug("add traversable " +url +"->" +uniquePrefix);
		// println("-- allow downloads that start with " + uniquePrefix + " --");
		// traversable(uniquePrefix);
		// PrefixPhrase prefixPhrase = traversablePrefixes.add(purl);
		// StringBuilder buffy = new StringBuilder("-- allow downloads that start with ");
		// prefixPhrase.toStringBuilder(buffy, traversablePrefixes.separator());
		// buffy.append(" --");
		// println(buffy);
		if ((ignoreReject || this.isNotReject(purl)) &&
				!traversablePrefixes.match(purl)) //If this purl already exists in traversablePrefixes, don't add/
			recordPrefix(traversablePrefixes, purl, "-- allow downloads that start with ");
	}

	private void recordPrefix(PrefixCollection prefixCollection, ParsedURL purl, String message)
	{
		// String uniquePrefix = purl.directoryString();
		// debug("add traversable " +url +"->" +uniquePrefix);
		// println("-- allow downloads that start with " + uniquePrefix + " --");
		// traversable(uniquePrefix);
		PrefixPhrase prefixPhrase = prefixCollection.add(purl);
		StringBuilder buffy = new StringBuilder(message);
		prefixPhrase.toStringBuilder(buffy, traversablePrefixes.separator());
		buffy.append(" --");
		Debug.println(buffy);
	}

	public boolean isNotReject(ParsedURL purl)
	{
		String domain = purl.domain();
		boolean result = domain != null;
		if (result)
		{
			result = !rejectDomains.contains(domain);
		}
		if (result)
		{
			result = !purl.isUnsupported();
		}
		if (!result)
			warning("Rejecting navigation to " + purl);

		return result;
	}

	/**
	 * Define the directory of the purl as a prefix that will not be crawled to, if limit_traveral is
	 * on.
	 */
	public void untraversable(ParsedURL purl)
	{
		// String uniquePrefix = purl.directoryString();
		// // debug("add traversable " +url +"->" +uniquePrefix);
		// if (!untraversableURLStrings.contains(uniquePrefix))
		// {
		// println("-- refusing downloads that start with " + uniquePrefix + " --");
		// untraversableURLStrings.addElement(uniquePrefix);
		// }
		recordPrefix(untraversablePrefixes, purl, "-- refuse downloads that start with ");
	}

	public void beginSeeding()
	{
		synchronized (SEEDING_STATE_LOCK)
		{
			if (!duringSeeding)
			{
				debug("beginSeeding() pause crawler");
				duringSeeding = true;
				semanticsSessionScope.getDownloadMonitors().pauseRegular(true);
				if (crawler != null)
					crawler.pause();
			}
		}
	}

	/**
	 * Called when seeding is complete.
	 */
	public void endSeeding()
	{
		synchronized (SEEDING_STATE_LOCK)
		{
			if (duringSeeding)
			{
				duringSeeding = false;
				debug("endSeeding() unpause crawler");
				semanticsSessionScope.getDownloadMonitors().pauseRegular(false);
				if (crawler != null)
					crawler.start();	// start thread *or* unpause()
			}
		}
	}

	public Collection<String> traversableURLStrings()
	{
		return traversablePrefixes.values();
	}

	public Collection<String> untraversableURLStrings()
	{
		return untraversablePrefixes.values();
	}

	public Collection<String> rejectDomainsCollection()
	{
		return rejectDomains;
	}

	public int numSeeds()
	{
		return (seedSet == null) ? -1 : seedSet.size();
	}

	// Accessors for InfoCollectorState
	// ///////////////////////////////////////////////////////
	public float adsBias()
	{
		return adsBias;
	}

	/**
	 * Are we willing to accept this url into one of the agent's candidate pools?
	 *
	 * @return	-1 if this url is null or from a domain we're rejecting.
	 * 		0  if this url is null or from a domain we go justFollow for.
	 * 		1  if this url is null or from a domain we're accepting.
	 */
	public boolean accept(ParsedURL purl)
	{
		if (acceptAll)
			return true;
		
		boolean result	= !(purl.url().getProtocol().equals("https://"));
		if (result)
		{
			result		= isNotReject(purl);
			if (result)
			{
				result	= !untraversablePrefixes.match(purl);
				if (result && LIMIT_TRAVERSAL.value())
				{
					result	= traversablePrefixes.match(purl);
				}
			}
		}
//		if (!result)
//			debug("accept() NOT " + purl);
		return result;
	}

	

	public void trackFirstSeedSet(SeedSet seedSet)
	{
		// TODO Auto-generated method stub
		
	}

	public void setPlayOnStart(boolean b)
	{
		this.playOnStart	= b;
	}

	public void clear()
	{
		// TODO Auto-generated method stub
		
	}


	public void setCurrentFileFromUntitled(File file)
	{
		// TODO Auto-generated method stub
		
	}
	public SeedPeer constructSeedPeer(Seed seed)
	{
		return null;
//		return DASHBOARD_ENABLED ? new SeedPeerDashboardOperand(seed, this) : null;
	}

	public void reject(String siteAddr)
	{
		if (siteAddr != null)
		{
			String domain	= StringTools.domain(siteAddr);
			if (domain != null)
			{
				rejectDomains.add(domain);
				Debug.println("-- rejecting all web addresses from domain "+domain+ " --");
			}
		}
	}
	
	public SeedSet getSeedSet()
	{
		SeedSet result = this.seedSet;
		if (result == null)
		{
			result = new SeedSet();
			this.seedSet = result;
		}
		return result;
	}

	public void clearSeedSet()
	{
		if(seedSet != null)
			seedSet.clear();
	}
	
	public void addSeeds(SeedSet<? extends Seed> newSeeds)
	{
		
		if (this.seedSet == null)
			this.seedSet = newSeeds;
		else
		{
			if (!newSeeds.isEmpty())
			{
				for (Seed seed: newSeeds)
				{
					this.seedSet.add(seed, semanticsSessionScope);
				}
			}
		}
	}

	public void getMoreSeedResults()
	{
		if (seedSet != null)
		{
			System.out.println(this + ".getMoreSeedResults()!!! " + seedSet.getStartingResultNum());
			seedSet.performNextSeeding(semanticsSessionScope);
		}
	}

	public SeedDistributor getSeedDistributor()
	{
		return seedSet.seedDistributer(semanticsSessionScope);
	}
	
	/**
	 * @return the duringSeeding
	 */
	public boolean isDuringSeeding()
	{
		return duringSeeding;
	}
	/**
	 * @return the heterogeneousSearchScenario
	 */
	public boolean isHeterogeneousSearchScenario()
	{
		return heterogeneousSearchScenario;
	}
	/**
	 * @return the playOnStart
	 */
	public boolean isPlayOnStart()
	{
		return playOnStart;
	}

	////////////////////////////////////////// end seeding stuff //////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}
