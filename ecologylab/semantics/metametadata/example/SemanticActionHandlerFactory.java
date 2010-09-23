package ecologylab.semantics.metametadata.example;

import ecologylab.semantics.actions.SemanticActionHandler;

/**
 * This factory interface allows you to customize MyInfoCollector with your own
 * SemanticActionHandler.
 * 
 * @author quyin
 * 
 */
public interface SemanticActionHandlerFactory
{
	SemanticActionHandler create();
}
