/**
 * 
 */
package ecologylab.documenttypes;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.ElementState;
import ecologylab.xml.XMLTranslationException;

/**
 * This class is used for directly biniding the XML document with metadata classes to build metadata
 * objects.
 * 
 * @author amathur
 * 
 */
public class MetaMetadataDirectBindingParser<M extends Metadata, SA extends SemanticAction>
		extends MetaMetadataParserBase
{

	public MetaMetadataDirectBindingParser(SemanticActionHandler semanticActionHandler,
			InfoCollector infoCollector)
	{
		super(infoCollector, semanticActionHandler);
	}

	@Override
	public M buildMetadataObject()
	{
		M populatedMetadata = null;

		if (metaMetadata.isSupported(container.purl()))
		{
			try
			{
				populatedMetadata = (M) ElementState.translateFromXML(inputStream(), getMetadataTranslationScope());
			}
			catch (XMLTranslationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return populatedMetadata;
	}

	
	/**
	 * This method is called for dierct binding only if it has some variables as it is expensive
	 * @param purl
	 */
	protected void createDOMandParse(ParsedURL purl)
	{
		org.w3c.dom.Document document	= ElementState.buildDOM(purl);
		semanticActionHandler.getSemanticActionReturnValueMap().put(SemanticActionsKeyWords.DOCUMENT_ROOT_NODE, document);
	}
}
