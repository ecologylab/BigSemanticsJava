/**
 * 
 */
package ecologylab.semantics.actions;

/**
 * This class contains some standard semantic methods for semantic actions. This basically contains
 * all the high level semantic actions which can be used.
 * 
 * @author amathur
 * 
 */

public interface SemanticActionStandardMethods
{

	/**
	 * conditional branching with only one branch, without <code>else</code>. to use <code>else</code>
	 * , you need to use <code>choose</code>.
	 */
	public static String	IF																	= "if";

	/**
	 * conditional branching with multiply branches.
	 */
	public static String	CHOOSE															= "choose";

	/**
	 * used in <code>choose</code> as the default branch.
	 */
	public static String	OTHERWISE														= "otherwise";

	/**
	 * looping.
	 */
	public static String	FOR_EACH														= "for_each";

	/**
	 * get field of an object and put it in the variable map so that following actions can access it.
	 */
	public static String	GET_FIELD_ACTION										= "get_field";

	/**
	 * set field of an object.
	 */
	public static String	SET_FIELD_ACTION										= "set_field";

	/**
	 * set metadata for an object
	 */
	public static String	SET_METADATA												= "set_metadata";

	/**
	 * link handling action.
	 */
	public static String	PARSE_DOCUMENT											= "parse_document";

	/**
	 * image handling action.
	 */
	public static String	CREATE_AND_VISUALIZE_IMG_SURROGATE	= "create_and_visualize_img_surrogate";

	/**
	 * text handling action.
	 */
	public static String	CREATE_AND_VISUALIZE_TEXT_SURROGATE	= "create_and_visualize_text_surrogate";

	/**
	 * start a new search.
	 */
	public static String SEARCH = "search";
	/**
	 * prevent info collector from obtaining info from a site.
	 */
	public static String	BACK_OFF_FROM_SITE									= "back_off_from_site";

	public static String	CREATE_SEMANTIC_ANCHOR							= "create_semantic_anchor";

	public static String	EVALUATE_RANK_WEIGHT								= "eval_rank_wt";

}
