package ecologylab.semantics.downloaders.oodss;

import ecologylab.collections.Scope;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.serialization.annotations.simpl_composite;

public class LookupMmdResponse extends ResponseMessage
{
	@simpl_composite
	public MetaMetadata metaMetadata;
		
	public LookupMmdResponse() {}
	
	public LookupMmdResponse (MetaMetadata metaMetadata)
	{
		this.metaMetadata = metaMetadata;
	}
	
	/** 
	 * Called automatically by OODSS on client
	 * */
	@Override
	public void processResponse(Scope appObjScope)
	{

	}
	
	/** 
	 * Checks that the message does not have an error condition,
	 *  for now we assume it doesn't */
	@Override
	public boolean isOK()
	{
		return true;
	}
}
