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
public class MetaMetadataXPathType<M extends Metadata, SA extends SemanticAction> extends
		MetaMetadataDocumentTypeBase implements SemanticActionsKeyWords
{
	
	public MetaMetadataXPathType(InfoCollector infoCollector)
	{
		super(infoCollector);
	}

	public MetaMetadataXPathType(SemanticActionHandler semanticActionHandler,
			InfoCollector infoCollector)
	{
		super(infoCollector, semanticActionHandler);
	}

	@Override
	public M buildMetadataObject()
	{
		truePURL 						= container.purl();
		if (metaMetadata.isSupported(truePURL))
		{
			recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
					container.metadata(), xpath, semanticActionHandler.getParameter(),document);
			//container.setMetadata(populatedMetadata);
		}
		
		return (M)container.metadata();
	}
}
