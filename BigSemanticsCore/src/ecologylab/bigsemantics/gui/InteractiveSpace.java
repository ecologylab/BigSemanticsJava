/**
 * 
 */
package ecologylab.bigsemantics.gui;

import ecologylab.bigsemantics.metadata.builtins.ImageClipping;
import ecologylab.bigsemantics.metadata.builtins.TextClipping;
import ecologylab.bigsemantics.model.text.TermWithScore;

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
