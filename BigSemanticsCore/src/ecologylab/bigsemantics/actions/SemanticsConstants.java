/**
 * 
 */
package ecologylab.bigsemantics.actions;

/**
 * Keywords used in variable modules.
 * 
 * @author amathur
 */
public interface SemanticsConstants
{

  final static String DOCUMENT_TYPE                   = "documentType";

  final static String CONTAINER                       = "container";

  final static String INFO_COLLECTOR                  = "infoCollector";

  final static String INFO_COLLECTOR_DATA_TYPE        = "InfoCollector";

  final static String METADATA                        = "metadata";

  final static String NOT_NULL_CHECK                  = "NOT_NULL";

  /**
   * Used for methods with boolean value
   */
  final static String METHOD_CHECK                    = "METHOD_CHECK";

  /**
   * To specify that we have to take action on a collection
   */
  final static String COLLECTION                      = "collection";

  /**
   * Key word for the current collection object of the loop. ie the kth object. This is not
   * avaiableto user.
   */
  final static String CURRENT_COLLECTION_INDEX        = "current-collection-index";

  final static String NODE_SET                        = "node_list";

  /**
   * Root Node of the document
   */
  final static String DOCUMENT_ROOT_NODE              = "__DocumentRootNode__";

  final static String NODE                            = "node";

  final static String TRUE                            = "true";

  final static String FALSE                           = "false";

  final static String NULL                            = "null";

  final static String TRUE_PURL                       = "TRUE_PURL";

  final static String DIRECT_BINDING_PARSER           = "direct";

  final static String XPATH_PARSER                    = "xpath";

  final static String HTML_IMAGE_DOM_TEXT_PARSER      = "html_dom_image_text";

  final static String FILE_DIRECTORY_PARSER           = "file_directory";

  final static String FEED_PARSER                     = "feed";

  final static String PDF_PARSER                      = "pdf";

  final static String IMAGE_PARSER                    = "image";

  final static String PURLCONNECTION_MIME             = "purl_connect_mime";

  final static String DOCUMENT_CALLER                 = "document_caller";

  final static String SURROUNDING_META_METADATA_STACK = "__SurroundingMetaMetadataStack__";

  final static String ELEMENT_INDEX_IN_COLLECTION     = "element_index_in_collection";

}
