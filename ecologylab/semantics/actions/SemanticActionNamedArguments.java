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
	public static final String	CONTAINER							= "container";

	/**
	 * Argument which tells the link for a contianer. Used in 1) CreateContainerForSearch 2)
	 * CreateContainer 3) ProcessSearch
	 */
	public static final String	CONTAINER_LINK				= "container_link";

	/**
	 * Tells the field value to be set. Used in 1) Setter Semantic action 2) SetMetadata
	 */
	public static final String	FIELD_VALUE						= "field_value";

	public static final String	IMAGE_PURL						= "image_purl";

	public static final String	CAPTION								= "caption";

	public static final String	HREF									= "href";

	public static final String	WIDTH									= "width";

	public static final String	HEIGHT								= "height";

	/**
	 * 1) CreateContainer
	 */
	public static final String	ANCHOR_TEXT						= "anchor_text";
	
	/**
	 * 1) CreateContainer
	 */
	public static final String ANCHOR_CONTEXT					= "anchor_context";

	/**
	 * 1) CreateContainer
	 */
	public static final String	ENTITY								= "entity";

	/**
	 * 1) CreateContainer
	 */
	public static final String	DOCUMENT							= "document";

	/**
	 * 1) CreateContainer
	 */
	public static final String	CITATION_SIGNIFICANCE	= "citation_sig";
	
	/**
	 * 1) CreateContainer
	 */
	public static final String  SIGNIFICANCE_VALUE		= "sig_value";
	
	/**
	 * 1) CreateContainer
	 */
	public static final String  TRAVERSABLE           = "traversable";
	
	public static final String INDEX									= "index";
	
	public static final String SIZE										= "size";
	
	
	/**
	 *  CreateVisualizeTextSurrogate
	 */
	public static final  String TEXT									= "text";
	
	public static final String SEMANTIC_TEXT					= "semantic_text";
	
	public static final String DOMAIN									= "domain";

}
