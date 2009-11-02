/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.xml.ElementState;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 * 
 */

public @xml_tag("arg") class Argument extends ElementState {

	public Argument()
	{
		super();
	}
	/**
	 * The value of the argument
	 */
	@xml_attribute
	private String value;

	/**
	 * The name of the argument
	 */
	@xml_attribute
	private String name;

	/**
	 * The check to be performed for this argument.
	 */
	@xml_attribute
	private String check;

	@xml_attribute
	private String context;

	@xml_attribute
	private boolean isNested;

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
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
}
