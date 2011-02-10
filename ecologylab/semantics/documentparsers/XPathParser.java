/**
 * 
 */
package ecologylab.semantics.documentparsers;

import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.connectors.InfoCollector;
import ecologylab.semantics.metadata.builtins.Document;

/**
 * @author amathur
 * 
 */
@SuppressWarnings("rawtypes")
public class XPathParser< SA extends SemanticAction> extends
		ParserBase implements SemanticActionsKeyWords
{
	
	public XPathParser(InfoCollector infoCollector)
	{
		super(infoCollector);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ecologylab.semantics.metadata.builtins.Document populateMetadata(SemanticActionHandler handler)
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
					container.getMetadata(), xpath, handler.getSemanticActionVariableMap(), getDom());

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
		
		return (Document)container.getMetadata();
	}
}
