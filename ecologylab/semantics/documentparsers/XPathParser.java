/**
 * 
 */
package ecologylab.semantics.documentparsers;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.serialization.SIMPLTranslationException;

/**
 * @author amathur
 * 
 */
public class XPathParser< SA extends SemanticAction> extends
		ParserBase implements SemanticActionsKeyWords
{
	
	public XPathParser(InfoCollector infoCollector)
	{
		super(infoCollector);
	}

	public XPathParser(InfoCollector infoCollector,SemanticActionHandler semanticActionHandler)
	{
		super(infoCollector, semanticActionHandler);
	}

	@Override
	public ecologylab.semantics.metadata.builtins.Document populateMetadata()
	{
		/*
		try
		{
			SimpleTimer.get("populating.log").startTiming(getContainer());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		recursiveExtraction(getMetadataTranslationScope(), metaMetadata,
					container.metadata(), xpath, semanticActionHandler.getSemanticActionVariableMap(),document);

		/*
		try
		{
			SimpleTimer.get("populating.log").finishTiming(getContainer());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		return container.metadata();
	}
}
