/**
 * 
 */
package ecologylab.semantics.metadata;

import ecologylab.semantics.metadata.builtins.Clipping;
import ecologylab.semantics.metadata.scalar.MetadataString;

/**
 * @author andruid
 *
 */
public class TextClipping extends Clipping
{
	@simpl_scalar
	MetadataString				text;
}
