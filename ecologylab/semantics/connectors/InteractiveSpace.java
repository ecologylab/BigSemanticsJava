/**
 * 
 */
package ecologylab.semantics.connectors;

/**
 * Wraps something like a CompositionSpace.
 * 
 * @author andruid
 *
 */
public interface InteractiveSpace
{

	void pauseIfPlaying();

	void waitIfPlaying();

	void pausePipeline();

	void restorePlayIfWasPlaying();

	void unpausePipeline();
	
}
