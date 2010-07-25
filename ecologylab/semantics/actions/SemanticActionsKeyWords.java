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
public interface SemanticActionsKeyWords
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

	public static String	NODE_SET									= "node_list";

	/**
	 * Root Node of the document
	 */
	public static String	DOCUMENT_ROOT_NODE				= "DocumentRootNode";

	public static String	NODE											= "node";

	public static String	TRUE											= "true";

	public static String	FALSE											= "false";

	public static String	NULL											= "null";

	public static String	TRUE_PURL									= "TRUE_PURL";

	public static String  DIRECT_BINDING_PARSER			= "direct";
	
	public static String  XPATH_PARSER							= "xpath";
	
	public static String  DEFAULT_PARSER						= "default";
	
	public static String  FEED_PARSER								= "feed";
	
	public static String	PDF_PARSER								= "pdf";
	
	public static String	IMAGE_PARSER							= "image";
	
	public static String PURLCONNECTION_MIME				= "purl_connect_mime";
}
