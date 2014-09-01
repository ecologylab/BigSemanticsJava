package ecologylab.bigsemantics.actions;

import java.io.IOException;
import java.util.Map;

import ecologylab.bigsemantics.documentparsers.DocumentParser;
import ecologylab.bigsemantics.documentparsers.ParserBase;
import ecologylab.bigsemantics.metadata.builtins.RichDocument;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataSelector;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_tag;

@simpl_inherit
@simpl_tag(SemanticActionStandardMethods.RESELECT_METAMETADATA_AND_EXTRACT)
public class ReselectAndExtractMetadataSemanticAction extends SemanticAction
{

	@Override
	public String getActionName()
	{
		return SemanticActionStandardMethods.RESELECT_METAMETADATA_AND_EXTRACT;
	}

	@Override
	public void handleError()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object perform(Object obj) throws IOException
	{
		RichDocument doc = (RichDocument) obj;
		MetaMetadata mmd = (MetaMetadata) doc.getMetaMetadata();
		Map<MetaMetadataSelector, MetaMetadata> reselectMap = mmd.getReselectMap();
		if (reselectMap != null)
		{
			for (MetaMetadataSelector selector : reselectMap.keySet())
			{
				if (selector.reselect(doc))
				{
					MetaMetadata newMmd = reselectMap.get(selector);
					DocumentParser newParser =
					    DocumentParser.getByMmd(newMmd,
					                            sessionScope,
					                            doc.getOrConstructClosure(),
					                            documentParser.getDownloadController());
					if (documentParser instanceof ParserBase && newParser instanceof ParserBase)
					{
						RichDocument newDoc = (RichDocument) newMmd.constructMetadata();
						newDoc.setLocation(doc.getLocation());
						ParserBase newParserBase = (ParserBase) newParser;
						org.w3c.dom.Document dom = ((ParserBase) documentParser).getDom();
						newParserBase.parse(newDoc, newMmd, dom);

						DocumentClosure closure = doc.getOrConstructClosure();
						closure.changeDocument(newDoc);

						return newDoc;
					}
				}
			}
		}
		return null;
	}

}
