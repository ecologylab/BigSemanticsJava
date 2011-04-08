/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.metadata.builtins.Document;

/**
 * @author amathur
 * 
 */
@SuppressWarnings("rawtypes")
public class XPathParser extends ParserBase implements
		SemanticActionsKeyWords
{

	public XPathParser(NewInfoCollector infoCollector)
	{
		super(infoCollector);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Document populateMetadata(SemanticActionHandler handler)
	{
		Document document = getDocument();
		recursiveExtraction(getMetaMetadata(), document, getDom(), null,
				handler.getSemanticActionVariableMap());
		return document;
	}
}
