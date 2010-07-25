/**
 * 
 */
package ecologylab.semantics.documentparsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.serialization.SIMPLTranslationException;

/**
 * @author amathur
 * 
 */
public class MetaMetadataXPathParser< SA extends SemanticAction> extends
		MetaMetadataParserBase implements SemanticActionsKeyWords
{
	
	public MetaMetadataXPathParser(InfoCollector infoCollector)
	{
		super(infoCollector);
	}

	public MetaMetadataXPathParser(InfoCollector infoCollector,SemanticActionHandler semanticActionHandler)
	{
		super(infoCollector, semanticActionHandler);
	}

	@Override
	public ecologylab.semantics.metadata.builtins.Document populateMetadata()
	{
		recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
					container.metadata(), xpath, semanticActionHandler.getSemanticActionReturnValueMap(),document);

		return container.metadata();
	}
}
