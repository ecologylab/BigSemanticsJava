/**
 * 
 */
package ecologylab.bigsemantics.gui;

/**
 * @author andruid
 *
 */
public interface WindowSystemBridge
{
  public int getAppropriateFontIndex();
  
  public void displayStatus(String message);
  
  public void displayStatus(String message, int ticks);
  
  public int showOptionsDialog(String message, String title, String[] options, int initialOptionIndex);
}
