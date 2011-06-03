package ecologylab.semantics.actions;

import java.util.Map;

import ecologylab.semantics.documentparsers.DocumentParser;
import ecologylab.semantics.documentparsers.ParserBase;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataSelector;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.ElementState.xml_tag;

@simpl_inherit
@xml_tag(SemanticActionStandardMethods.RESELECT_METAMETADATA_AND_EXTRACT)
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
	public Object perform(Object obj)
	{
		CompoundDocument doc = (CompoundDocument) obj;
		MetaMetadata mmd = (MetaMetadata) doc.getMetaMetadata();
		Map<MetaMetadataSelector, MetaMetadata> reselectMap = mmd.getReselectMap();
		if (reselectMap != null)
		{
			for (MetaMetadataSelector selector : reselectMap.keySet())
			{
				if (selector.reselect(doc))
				{
					MetaMetadata newMmd = reselectMap.get(selector);
					DocumentParser newParser = DocumentParser.get(newMmd, infoCollector);
					newParser.fillValues(documentParser.purlConnection(), doc.getOrConstructClosure(), infoCollector);
					if (documentParser instanceof ParserBase && newParser instanceof ParserBase)
					{
						CompoundDocument newDoc = (CompoundDocument) newMmd.constructMetadata();
						newDoc.setLocation(doc.getLocation());
						ParserBase newParserBase = (ParserBase) newParser;
						org.w3c.dom.Document DOM = ((ParserBase) documentParser).getDom();
						newParserBase.doParse(newDoc, newMmd, DOM);

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
