package ecologylab.semantics.actions;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
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
					LinkWith linkWith = thisMmd.getLinkWiths().get(name);
					MetaMetadata linkedMmd = sessionScope.getMetaMetadataRepository().getByTagName(name);
					if (linkedMmd != null)
					{
						String id = linkWith.getById();
						ParsedURL purl = linkedMmd.generateUrl(id, metadata.getNaturalIdValue(id));
						// the generated purl may not be associated with linkedMmd! e.g. linkedMmd is a
						// citeseerx_summary, while generated purl is a citeseerx search.
						Document linkedDocument	= (Document) sessionScope.getOrConstructDocument(purl);

						linkedDocument.setSemanticsSessionScope(sessionScope);
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
