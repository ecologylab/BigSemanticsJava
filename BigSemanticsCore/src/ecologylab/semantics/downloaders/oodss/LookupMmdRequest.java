package ecologylab.semantics.downloaders.oodss;

import ecologylab.collections.Scope;
import ecologylab.net.ParsedURL;
import ecologylab.oodss.messages.HttpRequest;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.serialization.annotations.simpl_scalar;

public class LookupMmdRequest extends HttpRequest
{
	@simpl_scalar
	private ParsedURL url;
		
	public LookupMmdRequest() {}
		
	public LookupMmdRequest(ParsedURL url)
	{
		this.url = url;
	}
		
	@Override
	public LookupMmdResponse performService(Scope cSScope)
	{
		SemanticsSessionScope scope = (SemanticsSessionScope)cSScope.operativeParent();	
		return new LookupMmdResponse(scope.getMetaMetadataRepository().getDocumentMM(url));
	}
		
	public boolean isDisposable()
	{
		return true;
	}
}
