package ecologylab.semantics.actions;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.connectors.Container;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.LinkWith;
import ecologylab.semantics.metametadata.MetaMetadata;
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
				if (metadata.getMetaMetadata() instanceof MetaMetadata)
				{
					MetaMetadata thisMmd = (MetaMetadata) metadata.getMetaMetadata();
					LinkWith lw = thisMmd.getLinkWiths().get(name);
					MetaMetadata mmd = infoCollector.metaMetaDataRepository().getByTagName(name);
					if (mmd != null)
					{
						String id = lw.getById();
						ParsedURL purl = mmd.generateUrl(id, metadata.getNaturalIdValue(id));
						Container container = infoCollector.getContainer(null, null, mmd, purl, false, false, false);
						if (container != null) // container could be null if it is already recycled
						{
							container.queueDownload();
							metadata.pendingSemanticActionHandler = semanticActionHandler;
							semanticActionHandler.requestWaiting = true;
						}
					}
				}
			}
			return linkedMetadata;
		}
		return null;
	}
	
}
