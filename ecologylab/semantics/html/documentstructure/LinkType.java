package ecologylab.semantics.html.documentstructure;

/**
 * Signifies whether the links are trusted or not.
 * Use as a parameter to be passed to the parse_document semantic action.
 * @author damaraju
 *
 */
public enum LinkType 
{
	TRUSTED_SEMANTICS,
	WILD
}