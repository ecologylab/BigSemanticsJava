package ecologylab.semantics.library;

import ecologylab.model.TextChunkBase;
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

	@xml_attribute	TextChunkBase text;

	public TextChunkBase getText()
	{
		return text;
	}

	public void hwSetText(TextChunkBase text)
	{
		this.text = text;
		rebuildCompositeTermVector();
	}
	
	public void setText(TextChunkBase text)
	{
		this.text = text;
	}
}
