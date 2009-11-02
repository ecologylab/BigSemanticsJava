/**
 * 
 */
package ecologylab.semantics.actions;

/**
 * @author amathur
 * 
 */
public interface SemanticActionNamedArguments
{

	/**
	 * Argument which tells the search container for process_search semantic action. Used in 1)
	 * CreateContainerForSearch 2) ProcessSearch 3) QueueDocumentDownload
	 */
	public static String	CONTAINER							= "container";

	/**
	 * Argument which tells the link for a contianer. Used in 1) CreateContainerForSearch 2)
	 * CreateContainer 3) ProcessSearch
	 */
	public static String	CONTAINER_LINK				= "container_link";

	/**
	 * Tells the field value to be set. Used in 1) Setter Semantic action 2) SetMetadata
	 */
	public static String	FIELD_VALUE						= "field_value";

	public static String	IMAGE_PURL						= "image_purl";

	public static String	CAPTION								= "caption";

	public static String	HREF									= "href";

	public static String	WIDTH									= "width";

	public static String	HEIGHT								= "height";

	/**
	 * 1) CreateContainer
	 */
	public static String	ANCHOR_TEXT						= "anchor_text";
	
	/**
	 * 1) CreateContainer
	 */
	public static String ANCHOR_CONTEXT					= "anchor_context";

	/**
	 * 1) CreateContainer
	 */
	public static String	ENTITY								= "entity";

	/**
	 * 1) CreateContainer
	 */
	public static String	DOCUMENT							= "document";

	/**
	 * 1) CreateContainer
	 */
	public static String	CITATION_SIGNIFICANCE	= "citation_sig";
	
	/**
	 * 1) CreateContainer
	 */
	public static String  SIGNIFICANCE_VALUE		= "sig_value";
	
	/**
	 * 1) CreateContainer
	 */
	public static String  TRAVERSABLE           = "traversable";
	
	public static String INDEX									= "index";
	
	public static String SIZE										= "size";
	
	
	/**
	 *  CreateVisualizeTextSurrogate
	 */
	public static String TEXT										="text";
	
	

}
