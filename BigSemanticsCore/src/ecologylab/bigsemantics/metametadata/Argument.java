/**
 * 
 */
package ecologylab.bigsemantics.metametadata;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;
import ecologylab.serialization.types.element.IMappable;

/**
 * @author amathur
 * 
 */

public @simpl_tag("arg") class Argument extends ElementState 
implements IMappable
{

	public Argument()
	{
		super();
	}
	/**
	 * The name of the argument
	 */
	@simpl_scalar
	private String name;

	/**
	 * The value of the argument
	 */
	@simpl_scalar
	private String value;

	/**
	 * The value of the argument if the first value is null.
	 */
	@simpl_scalar
	private String altValue;

	/**
	 * The check to be performed for this argument.
	 */
	@simpl_scalar
	private String check;

	@simpl_scalar
	private String context;

	@simpl_scalar
	private boolean isNested;

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	public String getAltValue()
	{
		return altValue;
	}
	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the type
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.name = type;
	}

	/**
	 * @return the check
	 */
	public String getCheck() {
		return check;
	}

	/**
	 * @param check
	 *            the check to set
	 */
	public void setCheck(String check) {
		this.check = check;
	}

	/**
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * @return the isNested
	 */
	public boolean isNested() {
		return isNested;
	}

	/**
	 * @param isNested the isNested to set
	 */
	public void setNested(boolean isNested) {
		this.isNested = isNested;
	}

	public Object key()
	{
		return name;
	}
}
