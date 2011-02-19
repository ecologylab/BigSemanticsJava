/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.builtins.Document;

/**
 * @author amathur
 * 
 */
@SuppressWarnings("rawtypes")
public class XPathParser<SA extends SemanticAction> extends ParserBase implements
		SemanticActionsKeyWords
{

	public XPathParser(InfoCollector infoCollector)
	{
		super(infoCollector);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Document populateMetadata(SemanticActionHandler handler)
	{
		recursiveExtraction(metaMetadata, container.getMetadata(), getDom(), null,
				handler.getSemanticActionVariableMap());
		return (Document) container.getMetadata();
	}
}
