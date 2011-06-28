/**
 * 
 */
package ecologylab.semantics.documentparsers;

import java.io.IOException;

import org.w3c.dom.Node;

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
			org.w3c.dom.Node DOM, SemanticActionHandler handler)
	{
		recursiveExtraction(metaMetadata, document, DOM, null, handler.getSemanticActionVariableMap());
		return document;
	}

}
