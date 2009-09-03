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
		initailizeMetadataObjectBuilding();
		M populatedMetadata = (M) getMetadata();
		if (metaMetadata.isSupported(container.purl()))
		{
			recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
					populatedMetadata, xpath, semanticActionHandler.getParameter());
			container.setMetadata((ecologylab.semantics.metadata.builtins.Document) populatedMetadata);
		}
		try
		{
			populatedMetadata.translateToXML(System.out);
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return populatedMetadata;
	}
}
