/**
 * 
 */
package ecologylab.bigsemantics.documentparsers;

import java.io.IOException;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.serialization.XMLTools;

/**
 * This class is used for directly biniding the XML document with metadata classes to build metadata
 * objects.
 * 
 * @author amathur
 * 
 */
public class DirectBindingParser extends ParserBase<Document>
{

	public DirectBindingParser(SemanticsGlobalScope infoCollector)
	{
		super(infoCollector);
	}

	@Override
	public Document populateMetadata(Document document,
			MetaMetadataCompositeField metaMetadata, org.w3c.dom.Document DOM,
			SemanticActionHandler handler)
	 throws IOException
	{
		return directBindingPopulateMetadata();
	}

	// FIXME - i don't think this is ever called.
	@Override
	public org.w3c.dom.Document getDom()
	{
		return XMLTools.buildDOM(getTruePURL());
	}

}
