/**
 * 
 */
package ecologylab.semantics.gui;

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
  
  public void pressPlayWhenFirstMediaArrives();
	
}
