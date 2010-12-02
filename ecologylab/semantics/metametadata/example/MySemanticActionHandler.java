package ecologylab.semantics.metametadata.example;

import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.metadata.Metadata;


/**
 * This semantic handler is used for this example.
 * 
 * @author quyin
 * 
 */
public class MySemanticActionHandler extends SemanticActionHandler
{

	/**
	 * postSemanticActionsHook is invoked for every parsed metadata, after all the semantic actions
	 * are performed.
	 */
	@Override
	public void postSemanticActionsHook(Metadata metadata)
	{
		synchronized (DataCollector.collected)
		{
			DataCollector.collected.add(metadata);
		}
		debug("Data saved");
	}

}


