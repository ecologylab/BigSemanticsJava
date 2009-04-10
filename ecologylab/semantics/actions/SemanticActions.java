/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.documenttypes.Container;
import ecologylab.documenttypes.InfoCollector;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metadata.Metadata;

/**
 * @author amathur
 * 
 */
public interface SemanticActions {
	
	/**
	 * TODO replace with a  better and more meaning ful action name.
	 * @param ancestor
	 * @param metadata
	 * @param reincarnate
	 * @param infoCollector
	 */
	public void getAndPerhapsCreateConatiner(Container ancestor, Metadata metadata,
			boolean reincarnate, InfoCollector infoCollector);

}
