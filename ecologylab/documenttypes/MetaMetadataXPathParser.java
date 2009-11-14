/**
 * 
 */
package ecologylab.documenttypes;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.xml.XMLTranslationException;

/**
 * @author amathur
 * 
 */
public class MetaMetadataXPathParser<M extends Metadata, SA extends SemanticAction> extends
		MetaMetadataParserBase implements SemanticActionsKeyWords
{
	
	public MetaMetadataXPathParser(InfoCollector infoCollector)
	{
		super(infoCollector);
	}

	public MetaMetadataXPathParser(SemanticActionHandler semanticActionHandler,
			InfoCollector infoCollector)
	{
		super(infoCollector, semanticActionHandler);
	}

	@Override
	public M buildMetadataObject()
	{
		if (metaMetadata.isSupported(truePURL))
		{
			recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
					container.metadata(), xpath, semanticActionHandler.getSemanticActionReturnValueMap(),document);
			//container.setMetadata(populatedMetadata);
		}
		
		return (M)container.metadata();
	}
}
