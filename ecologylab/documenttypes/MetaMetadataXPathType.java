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
import ecologylab.semantics.metadata.MetadataBase;

/**
 * @author amathur
 * 
 */
public class MetaMetadataXPathType<M extends MetadataBase, SA extends SemanticAction> extends
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
		M populatedMetadata = null;
		initailizeMetadataObjectBuilding();
		if (metaMetadata.isSupported(container.purl()))
		{
			populatedMetadata = (M) recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
					(M) getMetadata(), xpath, semanticActionHandler.getParameter());
			container.setContainerMetadata((ecologylab.semantics.metadata.Document) populatedMetadata);
		}
		return populatedMetadata;
	}
}
