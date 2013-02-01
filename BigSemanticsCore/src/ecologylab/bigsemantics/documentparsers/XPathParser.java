/**
 * 
 */
package ecologylab.bigsemantics.documentparsers;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.actions.SemanticActionsKeyWords;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;

/**
 * @author amathur
 * 
 */
public class XPathParser extends ParserBase<Document> implements
		SemanticActionsKeyWords
{

	public XPathParser(SemanticsGlobalScope infoCollector)
	{
		super(infoCollector);
	}

	@Override
	public Document populateMetadata(Document document, MetaMetadataCompositeField metaMetadata,
			org.w3c.dom.Document DOM, SemanticActionHandler handler)
	{
		recursiveExtraction(metaMetadata, document, DOM, null, handler.getSemanticActionVariableMap());
		return document;
	}

}
