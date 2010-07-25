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
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
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
public class DirectBindingParser
		extends ParserBase
{

	public DirectBindingParser(InfoCollector infoCollector,SemanticActionHandler semanticActionHandler)
	{
		super(infoCollector, semanticActionHandler);
	}

	@Override
	public Document populateMetadata()
	{
		return directBindingPopulateMetadata();
	}
	
	/**
	 * This method is called for direct binding only.
	 * It creates a DOM, by parsing a 2nd time. This DOM would only be used if XPath parsing is used
	 * specifically in the definition of variables.
	 * @param purl
	 */
	@Override
	protected void createDOMandParse(ParsedURL purl)
	{
		// used to create a DOM 
		org.w3c.dom.Document document	= XMLTools.buildDOM(purl);
		semanticActionHandler.getSemanticActionReturnValueMap().put(SemanticActionsKeyWords.DOCUMENT_ROOT_NODE, document);
	}

}
