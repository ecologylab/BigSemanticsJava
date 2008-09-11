package ecologylab.semantics.library;

import ecologylab.model.TextChunkBase;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metadata.Media;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_nested;

/**
 * 
 * @author damaraju
 *
 */
@xml_inherit
public class Text extends Media
{
	@xml_attribute	MetadataString text;

	public String getText()
	{
		return text.getValue();
	}
	
	public void setText(String text)
	{
	  this.text.setValue(text);
	}
}
