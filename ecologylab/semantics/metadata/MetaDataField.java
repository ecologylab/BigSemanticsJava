package ecologylab.semantics.metadata;

import ecologylab.xml.ElementState;
import ecologylab.xml.types.element.Mappable;

public class MetaDataField extends ElementState implements Mappable<String>
{
	String name;
	
	public String key()
	   {
		   return name;
	   }
}
