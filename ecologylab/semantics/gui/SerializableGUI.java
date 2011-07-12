/**
 * 
 */
package ecologylab.semantics.gui;

import java.awt.Color;
import java.awt.Rectangle;

import ecologylab.generic.Bounds;
import ecologylab.serialization.ObservableElementState;

/**
 * @author andrew
 *
 */
public class SerializableGUI extends ObservableElementState
{
	public static final String SERIALIZABLE_GUI_TRANSLATIONS_NAME = "serializable_gui_translations";
	
	
	@simpl_scalar
	protected Rectangle extent = new Rectangle();
	
	@simpl_scalar
	protected Color bgcolor;
	
	/**
	 * A descriptive name for this GUI object.
	 * Typically derived from the id attribute in a GUIState object / XML element.
	 * <p/>
	 * The id field should be unique within a given executable.
	 */ 
	@simpl_scalar
	protected String id;
	
	/**
	 * turn us on/off for painting and events
	 */
	@simpl_scalar
	protected boolean active = true;
	
	@simpl_scalar
	protected boolean interactive = true;
	
	
	public SerializableGUI() 
	{ 
		
	}
	
	public SerializableGUI(String id)
	{
		this.id	= id;
	}
	
	public boolean isInteractive()
	{
		return interactive;
	}

	public void setInteractive(boolean interactive)
	{
		this.interactive = interactive;
	}
	
	/**
	  * @return Returns the unique id descriptor for this.
	  */
	 public String id() 
	 {
		 return id;
	 }
}
