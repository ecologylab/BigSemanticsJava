/**
 * 
 */
package ecologylab.semantics.actions;

import ecologylab.xml.ElementState;
import ecologylab.xml.ElementState.xml_collection;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 * TODO Flag checks can latter have nested semantic actions
 *
 */

public @xml_tag("flag_check")class FlagCheck extends ElementState
{

		public FlagCheck()
		{
			super();
		}
		
		/**
		 *  The name of the flags to be checked
		 */
		@xml_attribute private String value;

		/**
		 * @return the name
		 */
		public String getValue()
		{
			return value;
		}

		/**
		 * @param name the name to set
		 */
		public void setValue(String name)
		{
			this.value = name;
		}
}
