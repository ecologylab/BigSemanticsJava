/**
 * 
 */
package ecologylab.semantics.actions;

/**
 * 
 * TODO Probably remane this class.
 * 
 * @author amathur
 * 
 */
public class SemanticActionsKeyWords
{

	public static String	DOCUMENT_TYPE							= "documentType";

	public static String	CONTAINER									= "container";

	public static String	INFO_COLLECTOR						= "infoCollector";

	public static String	INFO_COLLECTOR_DATA_TYPE	= "InfoCollector";

	public static String	METADATA									= "metadata";

	public static String	NOT_NULL_CHECK						= "NOT_NULL";

	/**
	 * Used for methods with boolean value
	 */
	public static String	METHOD_CHECK							= "METHOD_CHECK";

	/**
	 * To specify that we have to take action on a collection
	 */
	public static String	COLLECTION								= "collection";

	/**
	 * Key word for the current collection object of the loop. ie the kth object. This is not
	 * avaiableto user.
	 */
	public static String	CURRENT_COLLECTION_INDEX	= "current-collection-index";


}
