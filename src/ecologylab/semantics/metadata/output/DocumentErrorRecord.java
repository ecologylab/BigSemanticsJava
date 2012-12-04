package ecologylab.semantics.metadata.output;

import ecologylab.serialization.annotations.simpl_scalar;

public class DocumentErrorRecord
{
	@simpl_scalar
	int erroCode;
	
	@simpl_scalar
	String errorMsg;
}
