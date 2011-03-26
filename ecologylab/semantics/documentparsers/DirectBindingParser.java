/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.connectors.NewInfoCollector;
import ecologylab.semantics.metadata.builtins.Document;
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

	public DirectBindingParser(NewInfoCollector infoCollector)
	{
		super(infoCollector);
	}

	@Override
	public Document populateMetadata(SemanticActionHandler handler)
	{
		Document populatedMetadata	= directBindingPopulateMetadata();
//		container.setMetadata(populatedMetadata);
		
		return populatedMetadata;
	}

	@Override
	protected org.w3c.dom.Document createDom()
	{
		return XMLTools.buildDOM(getTruePURL());
	}

}
