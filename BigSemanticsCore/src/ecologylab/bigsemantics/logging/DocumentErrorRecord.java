package ecologylab.bigsemantics.logging;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
public class DocumentErrorRecord
{

	@simpl_scalar
	String message;
	
	@simpl_scalar
	String stackTrace;

}
