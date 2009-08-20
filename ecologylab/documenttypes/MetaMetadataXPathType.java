/**
 * 
 */
package ecologylab.documenttypes;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.MetadataBase;

/**
 * @author amathur
 * 
 */
public class MetaMetadataXPathType<M extends MetadataBase,SA extends SemanticAction> extends MetaMetadataDocumentTypeBase
{

	private XPath	xpath;

	private Tidy	tidy;

	public MetaMetadataXPathType(InfoCollector infoCollector)
	{
		super(infoCollector);
		//FIXME -- consolidate reduntant code in one method, w MMSearchType
		tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		xpath = XPathFactory.newInstance().newXPath();
	}

	public MetaMetadataXPathType(SemanticActionHandler semanticActionHandler,
			InfoCollector infoCollector)
	{
		super(infoCollector, semanticActionHandler);
		tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		xpath = XPathFactory.newInstance().newXPath();
	}

	@Override
	public M buildMetadataObject()
	{
		M populatedMetadata = null;
		initailizeMetadataObjectBuilding();
		if (metaMetadata.isSupported(container.purl()))
		{
			Document tidyDOM = tidy.parseDOM(inputStream(), /*System.out*/null);
			
			// store this document root as standard method
			semanticActionHandler.getParameter().addParameter(SemanticActionsKeyWords.DOCUMENT_ROOT_NODE, tidyDOM);
			populatedMetadata = (M) recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
					(M) getMetadata(), tidyDOM, xpath);
			container.setContainerMetadata((ecologylab.semantics.metadata.Document) populatedMetadata);
		}
		return populatedMetadata;
	}
}
