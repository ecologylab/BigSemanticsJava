/**
 * 
 */
package ecologylab.semantics.metametadata;

import ecologylab.xml.ElementState;
import ecologylab.xml.ElementState.xml_tag;

/**
 * @author amathur
 * TODO Flag checks can latter have nested semantic actions
 *
 */
public @xml_tag("if")class IFclause extends ElementState
{

		public IFclause()
		{
			super();
		}
		
		/**
		 *  The name of the flags to be checked
		 */
		@xml_attribute private String name;

		/**
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name)
		{
			this.name = name;
		}
}
