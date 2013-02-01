package ecologylab.semantics.html.documentstructure;

/**
 * Signifies whether the links are trusted or not.
 * Use as a parameter to be passed to the parse_document semantic action.
 * @author damaraju
 *
 */
public enum LinkType 
{
	CITATION_SEMANTIC_ACTION,
	TRUSTED_SEMANTIC_ACTION,
	SITE_BOOSTED_SEMANTIC_ACTION,
	OTHER_SEMANTIC_ACTION,
	WILD_CONTENT_BODY,
	WILD
}