/**
 * 
 */
package ecologylab.semantics.gui;

import ecologylab.semantics.metadata.builtins.ImageClipping;
import ecologylab.semantics.metadata.builtins.TextClipping;
import ecologylab.semantics.model.text.TermWithScore;

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
	
	public void createAndAddClipping(ImageClipping imageClipping, int x, int y);

	public void createAndAddClipping(TextClipping textClipping, int x, int y);
	
	public TermWithScore[] getTermScoresAtPoint(int x, int y);

}
