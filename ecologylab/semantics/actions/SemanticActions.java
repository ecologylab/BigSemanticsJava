/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.documenttypes.AbstractContainer;
import ecologylab.documenttypes.InfoProcessor;
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
	public void getAndPerhapsCreateConatiner(AbstractContainer ancestor, Metadata metadata,
			boolean reincarnate, InfoProcessor infoCollector);

}
