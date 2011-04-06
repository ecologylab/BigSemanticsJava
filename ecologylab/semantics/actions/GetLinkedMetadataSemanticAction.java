package ecologylab.semantics.actions;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.LinkWith;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.old.OldContainerI;
import ecologylab.serialization.ElementState.xml_tag;
import ecologylab.serialization.simpl_inherit;

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
					LinkWith linkWith = thisMmd.getLinkWiths().get(name);
					MetaMetadata linkedMmd = infoCollector.metaMetaDataRepository().getByTagName(name);
					if (linkedMmd != null)
					{
						String id = linkWith.getById();
						ParsedURL purl = linkedMmd.generateUrl(id, metadata.getNaturalIdValue(id));
						Document linkedDocument	= (Document) infoCollector.getGlobalDocumentMap().getOrConstruct(linkedMmd, purl);
						if (linkedDocument != null)
						{
							linkedDocument.queueDownload();
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
