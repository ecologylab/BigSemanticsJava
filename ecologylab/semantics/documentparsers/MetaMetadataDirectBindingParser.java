/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.XMLTools;

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
				MetadataClassDescriptor metadataClassDescriptor = metaMetadata.getMetadataClassDescriptor();
				ElementState rootElement	= metadataClassDescriptor.getInstance();
				populatedMetadata = (Document) getMetadataTranslationScope().deserialize(inputStream(), rootElement);
			  populatedMetadata.serialize(System.out);
			  System.out.println();
			  /*
				//FIXME-- Is there an efficient way to find the root element?????
			  org.w3c.dom.Document doc = populatedMetadata.translateToDOM();
			  Element ele = doc.getDocumentElement();
				String tagName= ele.getTagName();
				*/
			  MetaMetadata mmd = populatedMetadata.getMetaMetadata();
				if(mmd!=null)	// should be always
				{
					if (metadataClassDescriptor.getDescribedClass().isAssignableFrom(mmd.getMetadataClassDescriptor().getDescribedClass()))
				  // need to choose the more specific one
						metaMetadata = mmd;

//					if (metaMetadata == null)
//					else
//						warning("abandon metaMetadata from deserialization since we already find the right one.");
				}
				else
					error("No meta-metadata in root after direct binding :-(");
				System.out.println();
			}
			catch (SIMPLTranslationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return populatedMetadata;
	}

	
	/**
	 * This method is called for direct binding only.
	 * It creates a DOM, by parsing a 2nd time. This DOM would only be used if XPath parsing is used
	 * specifically in the definition of variables.
	 * @param purl
	 */
	protected void createDOMandParse(ParsedURL purl)
	{
		// used to create a DOM 
		org.w3c.dom.Document document	= XMLTools.buildDOM(purl);
		semanticActionHandler.getSemanticActionReturnValueMap().put(SemanticActionsKeyWords.DOCUMENT_ROOT_NODE, document);
	}
}
