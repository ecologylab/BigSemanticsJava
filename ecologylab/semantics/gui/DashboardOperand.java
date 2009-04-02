package ecologylab.semantics.gui;

import java.util.HashMap;
import java.util.Map.Entry;

import ecologylab.collections.Scope;
import ecologylab.documenttypes.InfoProcessor;

public interface DashboardOperand  {
	/**
	 * Bring this element into the agent or directly into the composition.
	 * 
	 * @param infoCollector
	 * @return
	 */
	public void performSeedingSteps(InfoProcessor infoCollector);

	/**
	 * These are the necessary INTERNAL steps to process a seed. This should be
	 * overridden by each type of seed. This method is called by
	 * perfromSeedingSteps.
	 * 
	 * @param infoCollector
	 * @return
	 */
	public void performInternalSeedingSteps(InfoProcessor infoCollector);

	/**
	 * Check the validity of the element when it is created. This is default,
	 * and is expected to be overridden
	 * 
	 * @return validity of the element
	 */
	public boolean validate();

	/**
	 * create a new instance of this seed.
	 * 
	 * @param query
	 *            The query to use.
	 * @param type
	 *            The action/type of seed this is.
	 * @return
	 */
	public DashboardOperand newInstanceOf(String query, String type);

	/**
	 * Allows us to specify if there is a fixed query vocabulary for the
	 * operand. Each entry in the outer array, is another array with 2 entries:
	 * first entry is human readable, for seeding language and dashboard. Second
	 * entry is internal String that gets passed to the engine.
	 */
	public String[][] fixedQueryVocabulary();

	/**
	 * Allows us to get the current operand's USER vocabulary entry.
	 * 
	 * @return User formated string
	 */
	public String getFixedQueryVocabularySelection();

	/**
	 * Every dashboard operand MUST have an infoCollector.
	 * 
	 * @param infoCollector
	 */
	public void setInfoCollector(InfoProcessor infoCollector);

	/**
	 * The String the dashboard uses to show (usually the query, or the website,
	 * or the doc feed name, etc).
	 * 
	 * @return The search query.
	 */
	public String valueString();

	/**
	 * @param query
	 *            The query (or site/value) to be set for the seed.
	 */
	public boolean setValue(String value);

	/**
	 * The category the dashboard uses to show. (Also called the query type).
	 * 
	 * @return The search category. (or, if a SEARCH, then the general type, and
	 *         not the engine.)
	 */
	public String categoryString();

	/**
	 * Implement hierarchy of Object to Object relationships. One can create
	 * others. null means we ARE the parent!
	 */
	public DashboardOperand parentOperand();

	/**
	 * @return if the element is a parent of children elements.
	 */
	public boolean isParent();

	/**
	 * @return true if the element is muted, false otherwise
	 */
	public boolean isMuted();

	/**
	 * @return true if the element is normal, false otherwise
	 */
	public boolean isNormal();

	/**
	 * @return true if the element is solo, false otherwise
	 */
	public boolean isSolo();

	/**
	 * This allows us to refresh the status in the dashboard model.
	 * 
	 * This MUST occur after the visibility of an operand has changed.
	 */
	public void refreshDashboardStatus();

	/**
	 * Specifies the operations to perform when you want to delete this element
	 */
	public void deleteAll();

	/**
	 * Specifies the operations to perform when you want to refresh the
	 * on-screen elements linked to this element
	 */
	public void refreshScreenSurrogatesVisibility();

	/**
	 * @return the visibility of this element as a string.
	 */
	public Visibility getVisibility();

	/**
	 * Sets the initial visibility of this element, based upon query-able data.
	 * This is used when we first create the element
	 * 
	 * @return the visibility of this element as a string.
	 */
	public String getAndSetInitialVisibility();

	/**
	 * Sets the visibility of the item. This is to speed up visibility requests.
	 * 
	 * @param visibility
	 *            String of what the visibility is.
	 */
	public void setVisibility(Visibility visibility);

	/**
	 * The category the this element the dashboard uses to show.
	 * 
	 * @return The search engine category.
	 */
	public String detailedCategoryString();

	/**
	 * Is this allowed to be edited?
	 * 
	 * @return
	 */
	public boolean isEditable();

	/**
	 * Are we allowed to delete this?
	 * 
	 * @return
	 */
	// TODO make this removable
	public boolean isDeletable();

	/**
	 * Is this allows to change it's visibility?
	 * 
	 * @return
	 */
	public boolean canChangeVisibility();

	/**
	 * Are we allowed to set this to be rejected??
	 * 
	 * @return
	 */
	public boolean isRejectable();

	/**
	 * informs the dashboard if this particular operand type has statistics
	 * counter associated with it. if true - then the stat counter will be
	 * enabled (container/image/text - for normal and mouseover) if false - then
	 * there will be no stat counter enabled. This does not mean you can't have
	 * stats. It just means that there is no counter display available.
	 * 
	 * @return
	 */
	public boolean hasStatDisplay();

	/**
	 * Number of containers associated with this "seed" This is only
	 * visible/used if hasStatDisplay() is true.
	 */
	public int numContainerCount();

	/**
	 * Number of text associated with this "seed" This is only visible/used if
	 * hasStatDisplay() is true.
	 */
	public int numTextCount();

	/**
	 * Number of images associated with this "seed" This is only visible/used if
	 * hasStatDisplay() is true.
	 */
	public int numImageCount();

	/**
	 * Total Number of containers associated with the agent This is only
	 * visible/used if hasStatDisplay() is true.
	 */
	public int numTotalContainerCount();

	/**
	 * Total Number of text associated with the agent This is only visible/used
	 * if hasStatDisplay() is true.
	 */
	public int numTotalTextCount();

	/**
	 * Total Number of images associated with the agent This is only
	 * visible/used if hasStatDisplay() is true.
	 */
	public int numTotalImageCount();

	/**
	 * The current value of the dashboard_enabled PrefBoolean, obtained
	 * dynamically via call by reference.
	 */
	public boolean isModelListenerEnabled();
}
