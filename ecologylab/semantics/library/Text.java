package ecologylab.semantics.library;

import ecologylab.model.TextChunkBase;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.xml.ElementState.xml_nested;

/**
 * 
 * @author damaraju
 *
 */
public class Text extends Metadata
{

	@xml_nested	TextChunkBase text;

	public TextChunkBase getText()
	{
		return text;
	}

	public void setText(TextChunkBase text)
	{
		this.text = text;
	}
}
