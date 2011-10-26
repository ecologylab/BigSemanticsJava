package ecologylab.semantics.actions;
import java.io.IOException;

import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.actions.SemanticActionStandardMethods;
import ecologylab.semantics.gui.InteractiveSpace;

/**
 * 
 */

/**
 * @author andruid
 *
 */
public class VisualizeClippings extends SemanticAction
implements SemanticActionStandardMethods
{

	/**
	 * 
	 */
	public VisualizeClippings()
	{
		
	}

	@Override
	public String getActionName()
	{
		return VISUALIZE_CLIPPINGS;
	}

	@Override
	public void handleError()
	{
		

	}

	@Override
	public Object perform(Object obj) throws IOException
	{
		InteractiveSpace interactiveSpace = sessionScope.getInteractiveSpace();
		if (documentParser.getDocumentClosure().isDnd() && interactiveSpace != null)
		{
			
		}
		return null;
	}

}
