package ecologylab.semantics.actions;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag(SemanticActionStandardMethods.GET_LINKED_METADATA)
public class GetLinkedMetadataSemanticAction extends SemanticAction
{
	
	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.GET_LINKED_METADATA;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj)
	{
		if (obj != null && obj instanceof Metadata)
		{
			Metadata metadata = (Metadata) obj;
			String name = getReturnObjectName();
			Metadata linkedMetadata = metadata.getLinkedMetadata(name);
			if (linkedMetadata == null)
			{
				// wait ...
				
			}
			return linkedMetadata;
		}
		return null;
	}

}
