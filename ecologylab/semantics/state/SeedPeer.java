package ecologylab.semantics.state;

import java.util.ArrayList;
import java.util.Iterator;

import ecologylab.documenttypes.AbstractContainer;
import ecologylab.documenttypes.InfoProcessor;
import ecologylab.generic.HashSetWriteSynch;
import ecologylab.generic.ReflectionTools;
import ecologylab.semantics.gui.DashboardOperand;
import ecologylab.semantics.gui.Visibility;

/**
 * Specification of a directive to the agent or otherwise to composition space
 * services.
 * 
 * @author andruid, robinson
 */
abstract public class SeedPeer extends ecologylab.services.messages.cf.Seed
		implements DashboardOperand {
	// these are used to facilitate the creation of our hashSets, so the set's
	// doesn't have to grow
	// as quickly within the first minute of use
	private static final int CONTAINER_COUNT_DEFAULT_SIZE = 100;
	protected static final int IMAGE_COUNT_DEFAULT_SIZE = 100;
	protected static final int TEXT_COUNT_DEFAULT_SIZE = 500;

	// action values
	protected static final String TRAVERSABLE = "traversable";
	protected static final String UNTRAVERSABLE = "untraversable";
	protected static final String REJECT = "reject";

	protected Visibility visibilityState = null; // muted,
	// solo,
	// normal, reject

	protected boolean transparentStatus = false;

	private HashSetWriteSynch<AbstractContainer> associatedContainers;

	private boolean inDeleteContainerProcess = false;
	protected boolean inDeleteSurrogateProcess = false;
	protected boolean inDeleteImageElementProcess = false;
	protected boolean inDeleteTextElementProcess = false;

	protected InfoProcessor infoCollector;
	protected ArrayList<SeedPeer> kids = new ArrayList<SeedPeer>(0);

	/**
	 * When set, indicates that the seed should be processed without using a
	 * {@link ResultDistributer ResultDistributer}.
	 */
	protected boolean noAggregator;

	protected boolean queueInsteadOfImmediate;

	/**
	 * Implement hierarchy of Seed to Seed relationships. One seed can create
	 * others.
	 */
	protected SeedPeer parent;

	public SeedPeer() {
	}

	protected SeedPeer(String name, String action) {
		this();
		weird("Instantiated Seed)" + name + "," + action
				+ ") but this should never be called directly");
	}

	protected SeedPeer(float bias) {
		this((SeedPeer) null, bias);
	}

	protected SeedPeer(SeedPeer ancestor, float bias) {
		this.bias = bias;
		this.parent = ancestor;
	}

	/**
	 * This sets the infoCollector for the seed, and then hands off the internal
	 * seeding processing to the individual seed type.
	 * 
	 * @param infoCollector
	 *            Must be non-null!
	 */
	public void performSeedingSteps(InfoProcessor infoCollector) {
		// desparately seeking non-null InfoCollector
		if (infoCollector != null)
			setInfoCollector(infoCollector);
		else if (this.infoCollector != null)
			infoCollector = this.infoCollector;
		if (infoCollector == null)
			throw new RuntimeException(
					"Null InfoCollector passed to performSeedingSteps() in "
							+ this);

		performInternalSeedingSteps(infoCollector);

		// TODO there is an ancestor for this seed now, so we will have also ADD
		// THIS (ie.. this
		// seed) to the ancestor
		// so that there is a link in both directions. The ancestor seed will
		// have a ArrayList
		// containing this info.
		// if the ArrayList is empty, then the it's not a parent to kids. If
		// it's not empty, then it
		// is a parent...
		if (parent != null)
			parent.addKids(this);
	}

	/**
	 * there is an ancestor for this seed now, so we will have also ADD THIS
	 * (ie.. this seed) to its ancestor so that there is a link in both
	 * directions. The ancestor seed will have a ArrayList containing this info.
	 * if the parent's ArrayList is empty, then it's not a parent to (all) kids.
	 * If it's not empty, then it is a parent...
	 * 
	 * @param seed
	 *            the seed that is a child of this
	 */
	private void addKids(SeedPeer seed) {
		kids.add(seed);
	}

	/**
	 * is this seed a parent of other seeds?
	 * 
	 * @return
	 */
	public boolean isParent() {
		return kids.size() > 0 ? false : true;
	}

	public void setInfoCollector(InfoProcessor infoCollector) {
		this.infoCollector = infoCollector;
	}

	public boolean validate() {
		return true;
	}

	/**
	 * @return TRUE if is it specifically set to NORMAL
	 */
	public boolean isNormal() {
		return Visibility.NORMAL.equals(visibilityState); // ||
															// transparentStatus;
	}

	/**
	 * @return TRUE if is it specifically set to SOLO
	 */
	public boolean isSolo() {
		return Visibility.SOLO.equals(visibilityState); // || transparentStatus;
	}

	/**
	 * A hack for search seeds. Base class implementation is a no-op.
	 */
	public void fixNumResults() {
	}

	/**
	 * This is sets the visibility to MUTED, where this seed is visible to the
	 * user. This is different than regular mute, since this is performed by a
	 * kid operation and is a TRANSPARENT operation. This is ONLY (and should
	 * be) called by the dashboard (or operation via the dashboard)!!!
	 */
	public void doMuteTransparent() {
		visibilityState = Visibility.MUTE;
		transparentStatus = true;
	}

	/**
	 * This is sets the visibility to SOLO, where this seed is visible to the
	 * user. This is different than regular solo, since this is performed by a
	 * kid operation and is a TRANSPARENT operation. This is ONLY (and should
	 * be) called by the dashboard (or operation via the dashboard)!!!
	 */
	public void doSoloTransparent() {
		visibilityState = Visibility.SOLO;
		transparentStatus = true;
	}

	/**
	 * This is sets the visibility to SOLO, where this seed is visible to the
	 * user. This is different than regular solo, since this is performed by a
	 * kid operation and is a TRANSPARENT operation. This is ONLY (and should
	 * be) called by the dashboard (or operation via the dashboard)!!!
	 */
	public void doNormalTransparent() {
		visibilityState = Visibility.NORMAL;
		transparentStatus = false;
	}

	/**
	 * This takes the parent (of the current seed) and iterates through the
	 * kids, and set's the parent to the correct status.
	 */
	protected void checkParent() {
		if (parent == null)
			return;

		// This is the schema for the transparent status of the parent.
		boolean kidsMuted = false;
		boolean kidsSolod = false;

		for (SeedPeer kid : parent.kids) {
			// if it's a transparent action, then we don't care about it.
			if (!kid.isTransparentStatus()) {
				if (kid.visibilityState.equals(Visibility.MUTE))
					kidsMuted = true;
				if (kid.visibilityState.equals(Visibility.SOLO))
					kidsSolod = true;
			}
		}

		// the parent can only reflect the status of ONE type of kid that it
		// has.
		// This is the order of precedence in determining it's status.
		if (kidsMuted)
			parent.doMuteTransparent();
		else if (kidsSolod)
			parent.doSoloTransparent();
		else
			parent.doNormalTransparent();
	}

	/**
	 * /** This deletes potential seeds from the download monitor
	 */
	private void deleteFromVisualPool() {
		// delete text to be downloaded in downloadMonitor (infocollector is
		// already pausing it.)
		// this stuff is added seperately to it's own deal.

		// recycling...
		// associatedElements.clear();
		// associatedElements = null;

		// inDeleteElementProcess = false;
	}

	/**
	 * This allows us to display the stat counter in the dashboard.
	 */
	public boolean hasStatDisplay() {
		return true;
	}

	/**
	 * adds to the inverted index. also increments the associated count.
	 * 
	 * @param container
	 *            Container to add to the associatedContainers inverted index.
	 */
	public void addToIndex(AbstractContainer container) {
		if (!isModelListenerEnabled())
			return;

		// System.err.println("STARTING ADD");
		if (!inDeleteContainerProcess) {
			if (associatedContainers == null)
				associatedContainers = new HashSetWriteSynch<AbstractContainer>(
						CONTAINER_COUNT_DEFAULT_SIZE);

			// this is where i add the inverted index thing for CONTAINERS.
			associatedContainers.add(container);
		}
	}

	/**
	 * @param container
	 *            Container to remove from the associatedContainers inverted
	 *            index.
	 */
	public void removeFromIndex(AbstractContainer container) {
		if (!isModelListenerEnabled())
			return;

		// this is where i REMOVE the CONTAINER from the inverted index.
		if (!inDeleteContainerProcess && associatedContainers != null)
			associatedContainers.remove(container);
	}

	/**
	 * If the engine only takes a fixed vocabulary of queries, this method must
	 * be overridden to return that vocabulary as an array. Each entry in the
	 * outer array, is another array with 2 entries: first entry is human
	 * readable, for seeding language and dashboard. Second entry is internal
	 * String that gets passed to the engine.
	 * 
	 * @return null, because in the default case, queries can be anything.
	 */
	public String[][] fixedQueryVocabulary() {
		return null;
	}

	/**
	 * Returns the USER readable fixed query vocabulary entry.
	 */
	public String getFixedQueryVocabularySelection() {
		return null;
	}

	/**
	 * delicious ONLY!
	 * 
	 * @return the creator (for delicious bookmark authors)
	 */
	public String getCreator() {
		return null;
	}

	/**
	 * @param category
	 *            The category (WEB_SITE, TRAVERSABLE, etc..) to be set for the
	 *            seed. if you are setting the category for a SEARCH, then this
	 *            value is what engine you want to use (GOOGLE, FLICKR, etc..)
	 */
	public abstract void setCategory(String value);

	/**
	 * @return Returns the bias.
	 */
	public float bias() {
		return bias;
	}

	/**
	 * returns what the visibility of this object is.
	 * 
	 * @return visibility
	 */
	public Visibility getVisibility() {
		return visibilityState;
		// if (valueString().equals(""))
		// return null;
		//		 
		// else if (visibilityState.equals(Visibility.SOLO))
		// return "solo";
		// else if (visibilityState.equals(Visibility.MUTE))
		// return "mute";
		// else if (visibilityState.equals(Visibility.REJECT))
		// return "reject";
		//		 
		// // we are unknown, so we return "normal" status...
		// return "normal";
	}

	/**
	 * This returns the initial (ie. starting) visibility of a seed.
	 * 
	 * @return normal, solo, mute, reject
	 */
	public String getAndSetInitialVisibility() {
		if (valueString() != null && !valueString().equals("")) {
			if (categoryString().equals(REJECT)) {
				setVisibility(Visibility.REJECT);
				return "reject";
			}
			setVisibility(Visibility.NORMAL);
			return "normal";
		}
		return "normal";
	}

	// Ok.. so, we want to know how many available elements in these categories
	// we have available.
	// i.e. how many images we have ON HAND. These are not candidates (pre
	// download), but rather
	// ones we actually have downloaded, and are resiging in memory.

	// this is to know the number of containers this seed has.
	public int numContainerCount() {
		int totalCount = 0;
		if (kids.size() > 0) {
			for (SeedPeer kidSeed : kids) {
				totalCount += kidSeed.numContainerCount();
			}
			return totalCount;
		}
		return associatedContainers == null ? 0
				: (totalCount + this.associatedContainers.size());
	}

	/**
	 * @return Returns the queueInsteadOfImmediate.
	 */
	public boolean queueInsteadOfImmediate() {
		return queueInsteadOfImmediate;
	}

	protected boolean noAggregator() {
		return this.noAggregator;
	}

	public void setVisibility(Visibility visibility) {
		visibilityState = visibility;
	}

	/**
	 * Implement hierarchy of Seed to Seed relationships. One seed can create
	 * others.
	 */
	public SeedPeer parentOperand() {
		return parent;
	}

	public boolean isTransparentStatus() {
		return transparentStatus;
	}

	static final Class<?>[] CONSTRUCTOR_ARG_TYPES = { String.class,
			String.class };

	public SeedPeer newInstanceOf(String query, String type) {
		String[] args = new String[2];
		args[0] = query;
		args[1] = type;
		return ReflectionTools.getInstance(this.getClass(),
				CONSTRUCTOR_ARG_TYPES, args);
	}

}