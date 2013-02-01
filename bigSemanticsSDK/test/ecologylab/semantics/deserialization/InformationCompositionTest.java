/**
 * 
 */
package ecologylab.semantics.deserialization;

import java.util.ArrayList;

import ecologylab.semantics.metadata.builtins.Clipping;
import ecologylab.semantics.metadata.builtins.ImageClipping;
import ecologylab.semantics.metadata.builtins.TextClipping;
import ecologylab.semantics.namesandnums.SemanticsNames;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_classes;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * @author andruid
 *
 */
@simpl_tag("information_composition")
public class InformationCompositionTest extends ElementState
implements SemanticsNames
{
	@simpl_scalar float version;
	
	@simpl_collection
//	@simpl_scope(SemanticsNames.REPOSITORY_CLIPPING_TRANSLATIONS)
	@simpl_classes({ImageClipping.class, TextClipping.class})
	ArrayList<? extends Clipping>	 clippings;
	
	/**
	 * 
	 */
	public InformationCompositionTest()
	{
		// TODO Auto-generated constructor stub
	}

}
