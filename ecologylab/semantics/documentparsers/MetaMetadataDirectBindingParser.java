/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.ElementState;
import ecologylab.xml.XMLTranslationException;

/**
 * This class is used for directly biniding the XML document with metadata classes to build metadata
 * objects.
 * 
 * @author amathur
 * 
 */
public class MetaMetadataDirectBindingParser<SA extends SemanticAction>
		extends MetaMetadataParserBase
{

	public MetaMetadataDirectBindingParser(InfoCollector infoCollector,SemanticActionHandler semanticActionHandler)
	{
		super(infoCollector, semanticActionHandler);
	}

	@Override
	public Document populateMetadataObject()
	{
		Document populatedMetadata = null;
		String mimeType = (String) semanticActionHandler.getSemanticActionReturnValueMap().get(SemanticActionsKeyWords.PURLCONNECTION_MIME);
		if (metaMetadata.isSupported(container.purl(), mimeType))
		{
			try
			{
				populatedMetadata = (Document) ElementState.translateFromXML(inputStream(), getMetadataTranslationScope());
			  populatedMetadata.translateToXML(System.out);
			  System.out.println();
			  /*
				//FIXME-- Is there an efficient way to find the root element?????
			  org.w3c.dom.Document doc = populatedMetadata.translateToDOM();
			  Element ele = doc.getDocumentElement();
				String tagName= ele.getTagName();
				*/
			  MetaMetadata mmd = populatedMetadata.getMetaMetadata();
				if(mmd!=null)
					metaMetadata = mmd;
				System.out.println();
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
