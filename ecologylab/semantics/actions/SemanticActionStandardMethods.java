/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.semantics.connectors.Container;

/**
 * This class contains some standard semantic methods for semantic actions. This bascially contains
 * all the high level semantic actions which can be used.
 * 
 * @author amathur
 * 
 */

public interface SemanticActionStandardMethods
{

	/**
	 * Used for looping. Implemented in MetaMetadata XPathtype
	 */
	public static String	FOR_EACH														= "for_each";

	/**
	 * Used for creating the container. TODO add more java doc here.
	 */
	public static String	CREATE_CONATINER										= "create-container";

	/**
	 * Creates a container for search.
	 */
	public static String	CREATE_CONTAINER_FOR_SEARCH					= "create_container_for_search";

	/**
	 * Creates and send a visual surrogate for visualization
	 */
	public static String	CREATE_AND_VISUALIZE_IMG_SURROGATE	= "create_and_visualize_img_surrogate";

	/**
	 * Used for setting metadata for a container/surrogate
	 */
	public static String	SET_METADATA												= "set-metadata";

	/**
	 * process a conatiner
	 */
	public static String	PROCESS_DOCUMENT										= "process-document";

	/**
	 * Create a new search
	 */
	public static String	CREATE_SEARCH												= "create-search";

	/**
	 * The action which can be called via reflection.
	 */
	public static String	GENERAL_ACTION											= "general-action";

	/**
	 * To get a particular field.
	 */
	public static String	GET_FIELD_ACTION												= "get_field";

	/**
	 * to set a paticular field
	 */
	public static String	SETTER_ACTION												= "set";

	/**
	 * to queue a search request. It may be same as create search action. Have to verify it.
	 */
	public static String	PROCESS_SEARCH								= "process_search";



}
