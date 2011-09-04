/**
 * 
 */
package ecologylab.semantics.gui;

import java.awt.Color;
import java.awt.Rectangle;

import ecologylab.serialization.ObservableElementState;
import ecologylab.serialization.simpl_inherit;

/**
 * @author andrew
 * 
 */
@simpl_inherit
public class SerializableGUI extends ObservableElementState
{
	public static final String	SERIALIZABLE_GUI_TRANSLATIONS_NAME	= "serializable_gui_translations";

	/**
	 * used by the ORM layer as the primary key field for database tables.
	 */
	private long								ormId;

	@simpl_scalar
	protected Rectangle					extent															= new Rectangle();

	@simpl_scalar
	protected Color							bgcolor;

	/**
	 * A descriptive name for this GUI object. Typically derived from the id attribute in a GUIState
	 * object / XML element.
	 * <p/>
	 * The id field should be unique within a given executable.
	 */
	@simpl_scalar
	protected String						id;

	/**
	 * turn us on/off for painting and events
	 */
	@simpl_scalar
	protected boolean						isActive															= true;

	@simpl_scalar
	protected boolean						isInteractive													= true;
	
	@simpl_scalar
	protected boolean						centered															= false;

	public SerializableGUI()
	{

	}

	public SerializableGUI(String id)
	{
		this.id = id;
	}

	public boolean isInteractive()
	{
		return isInteractive;
	}

	public boolean getIsInteractive()
	{
		return isInteractive;
	}

	public void setIsInteractive(boolean interactive)
	{
		this.isInteractive = interactive;
	}

	/**
	 * @return Returns the unique id descriptor for this.
	 */
	public String getId()
	{
		return id;
	}

	public long getOrmId()
	{
		return ormId;
	}

	public void setOrmId(long ormId)
	{
		this.ormId = ormId;
	}

	public Rectangle getExtent()
	{
		return extent;
	}

	public void setExtent(Rectangle extent)
	{
		this.extent = extent;
	}

	public Color getBgcolor()
	{
		return bgcolor;
	}

	public void setBgcolor(Color bgcolor)
	{
		this.bgcolor = bgcolor;
	}

	public boolean getIsActive()
	{
		return isActive;
	}

	public void setIsActive(boolean active)
	{
		this.isActive = active;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public boolean isCentered()
	{
		return centered;
	}

	public void setCentered(boolean centered)
	{
		this.centered = centered;
	}

}
