/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;

/**
 * @author amathur
 * 
 */
public class XPathParser extends ParserBase implements
		SemanticActionsKeyWords
{

	public XPathParser(NewInfoCollector infoCollector)
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
