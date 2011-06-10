/**
 * 
 */
package ecologylab.semantics.documentparsers;

import java.io.IOException;

import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.metadata.builtins.CompoundDocument;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.XMLTools;

/**
 * This class is used for directly biniding the XML document with metadata classes to build metadata
 * objects.
 * 
 * @author amathur
 * 
 */
public class DirectBindingParser extends ParserBase
{

	public DirectBindingParser(NewInfoCollector infoCollector)
	{
		super(infoCollector);
	}

	@Override
	public Document populateMetadata(CompoundDocument document,
			MetaMetadataCompositeField metaMetadata, org.w3c.dom.Document DOM,
			SemanticActionHandler handler)
	 throws IOException
	{
		return directBindingPopulateMetadata();
	}

	// FIXME - i don't think this is ever called.
	@Override
	protected org.w3c.dom.Document createDom()
	{
		return XMLTools.buildDOM(getTruePURL());
	}

}
