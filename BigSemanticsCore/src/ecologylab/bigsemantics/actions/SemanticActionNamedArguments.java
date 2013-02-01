/**
 * 
 */
package ecologylab.bigsemantics.actions;

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
	public static final String	CONTAINER								= "container";

	/**
	 * Argument which tells the link for a contianer. Used in 1) CreateContainerForSearch 2)
	 * CreateContainer 3) ProcessSearch
	 */
	public static final String	LOCATION								= "location";

	/**
	 * Tells the field value to be set. Used in 1) Setter Semantic action 2) SetMetadata
	 */
	public static final String	FIELD_VALUE							= "field_value";

	public static final String	IMAGE_PURL							= "image_purl";

	public static final String	CAPTION									= "caption";

	public static final String	DESCRIPTION							= "description";

	public static final String	HREF										= "href";

	public static final String	WIDTH										= "width";

	public static final String	HEIGHT									= "height";

	public static final String	HREF_METADATA						= "href_metadata";

	/**
	 * 1) CreateContainer
	 */
	public static final String	ANCHOR_TEXT							= "anchor_text";

	/**
	 * 1) CreateContainer
	 */
	public static final String	ANCHOR_CONTEXT					= "anchor_context";

	/**
	 * 1) CreateContainer
	 */
	public static final String	ENTITY									= "entity";

	/**
	 * 1) CreateContainer
	 */
	public static final String	DOCUMENT								= "document";

	public static final String	SOURCE_DOCUMENT					= "source_document";

	public static final String	MIXIN										= "mixin";

	/**
	 * 1) CreateContainer
	 */
	public static final String	CITATION_SIGNIFICANCE		= "citation_sig";

	/**
	 * 1) CreateContainer
	 */
	public static final String	SIGNIFICANCE_VALUE			= "sig_value";

	/**
	 * 1) CreateContainer
	 */
	public static final String	TRAVERSABLE							= "traversable";

	public static final String	IGNORE_CONTEXT_FOR_TV		= "ignore_context_for_tv";

	public static final String	INDEX										= "index";

	public static final String	CURRENT_INDEX						= "current_index";

	public static final String	SIZE										= "size";

	public static final String	OUTER_LOOP_INDEX				= "outer_loop_index";

	public static final String	OUTER_LOOP_SIZE					= "outer_loop_size";

	public static final String	NUMBER_OF_TOP_DOCUMENTS	= "number_of_top_documents";

	/**
	 * CreateVisualizeTextSurrogate
	 */
	public static final String	TEXT										= "text";

	public static final String	CONTEXT									= "context";

	public static final String	HTML_CONTEXT						= "html_context";

	public static final String	SEMANTIC_TEXT						= "semantic_text";

	public static final String	DOMAIN									= "domain";

	/**
	 * Used by set_field
	 */
	public static final String	VALUE										= "value";

	public static final String	LINK_TYPE								= "link_type";

	// parse_document
	public static final String	RANK										= "rank";

	public static final String	METADATA								= "metadata";

}
