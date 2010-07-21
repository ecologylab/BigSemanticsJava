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
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.serialization.DeserializationHookStrategy;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.XMLTools;

/**
 * This class is used for directly biniding the XML document with metadata classes to build metadata
 * objects.
 * 
 * @author amathur
 * 
 */
public class MetaMetadataDirectBindingParser<SA extends SemanticAction, M extends Metadata>
		extends MetaMetadataParserBase
		implements DeserializationHookStrategy<M>
{

	public MetaMetadataDirectBindingParser(InfoCollector infoCollector,SemanticActionHandler semanticActionHandler)
	{
		super(infoCollector, semanticActionHandler);
	}

	@Override
	public Document populateMetadataObject()
	{
		String mimeType = (String) semanticActionHandler.getSemanticActionReturnValueMap().get(
				SemanticActionsKeyWords.PURLCONNECTION_MIME);
		Document populatedMetadata	= null;
		if (metaMetadata.isSupported(container.purl(), mimeType))
		{
			try
			{
				//FIXME 
				populatedMetadata = (Document) getMetadataTranslationScope().deserialize(inputStream(), this);
				populatedMetadata.serialize(System.out);
				System.out.println();
//				bindMetaMetadataToMetadata(populatedMetadata);
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
	 * @param metadataFromDerialization
	 */
	private boolean bindMetaMetadataToMetadata(MetaMetadataField deserializationMM, MetaMetadataField originalMM)
	{
		if (deserializationMM != null) // should be always
		{
			MetadataClassDescriptor originalClassDescriptor 				= originalMM.getMetadataClassDescriptor();
			MetadataClassDescriptor deserializationClassDescriptor	= deserializationMM.getMetadataClassDescriptor();

			boolean sameMetadataSubclass 														= originalClassDescriptor.equals(deserializationClassDescriptor);
			// if they have the same metadataClassDescriptor, they can be of the same type, or one
			// of them is using "type=" attribute.
			boolean useMmdFromDeserialization 											= sameMetadataSubclass && (deserializationMM.getType() != null);
			if (!useMmdFromDeserialization && !sameMetadataSubclass)
				// if they have different metadataClassDescriptor, need to choose the more specific one
				useMmdFromDeserialization				= originalClassDescriptor.getDescribedClass().isAssignableFrom(
						deserializationClassDescriptor.getDescribedClass());
			return useMmdFromDeserialization ;
		}
		else
		{
			error("No meta-metadata in root after direct binding :-(");
			return false;
		}
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

	MetaMetadataField	currentMM;
	
	/**
	 * For the root, compare the meta-metadata from the binding with the one we started with.
	 * Down the hierarchy, try to perform similar bindings.
	 */
	@Override
	public void preDeserializationHook(M deserializedMetadata)
	{
		if (deserializedMetadata.parent() == null)
		{
			MetaMetadata deserializationMM = deserializedMetadata.getMetaMetadata();
			if (bindMetaMetadataToMetadata(deserializationMM, metaMetadata))
			{
				metaMetadata 										= deserializationMM;
			}
			else
			{
				deserializedMetadata.setMetaMetadata(metaMetadata);
			}
			currentMM	= metaMetadata;
		}
		else 
		{
			MetaMetadata deserializationMM		= deserializedMetadata.getMetaMetadata();
			if (currentMM != null)
			{
				MetaMetadataField newOriginalMMF	= currentMM.lookupChild(deserializationMM.getName());	// this fails for collections :-(
				if (false) // (newOriginalMMF != null)
				{
					if (!bindMetaMetadataToMetadata(deserializationMM, newOriginalMMF))
					{
						//TODO -- if this class cast fails, lookup equiv?! it must be a composite object for us to get here.
						//TODO -- are the types correct anyway? should it be MetaMetadata or MetaMetadataCompositeField in Metadata???
						deserializedMetadata.setMetaMetadata((MetaMetadata) newOriginalMMF);
					}
				}
				currentMM													= newOriginalMMF;
			}
		}
	}
}
