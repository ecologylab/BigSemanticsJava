/**
 * 
 */
package ecologylab.semantics.model;

import java.util.ArrayList;
import java.util.Iterator;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.types.scalar.ScalarType;

/**
 * @author andruid
 *
 */
public class TextChunk extends TextChunkBase<TextToken>
{

	/**
	 * @param doUnderline
	 * @param commonHref
	 */
	public TextChunk(boolean doUnderline, ParsedURL commonHref)
	{
		super(doUnderline, commonHref);
	}

	/**
	 * @param doUnderline
	 */
	public TextChunk(boolean doUnderline)
	{
		super(doUnderline);
	}

	/**
	 * @param doUnderline
	 * @param scalarType
	 */
	public TextChunk(boolean doUnderline, ScalarType scalarType)
	{
		super(doUnderline, scalarType);
	}

	/**
	 * @param doUnderlineArg
	 * @param untokenized
	 */
	public TextChunk(boolean doUnderlineArg, CharSequence untokenized)
	{
		super(doUnderlineArg, untokenized);
	}

	/**
	 * @param doUnderlineArg
	 * @param untokenized
	 * @param scalarType
	 */
	public TextChunk(boolean doUnderlineArg, CharSequence untokenized,
			ScalarType scalarType)
	{
		super(doUnderlineArg, untokenized, scalarType);
	}
	
	/**
	 * Factory method to call the correct constructor.
	 * @param doUnderlineArg
	 * @param untokenized
	 * @param scalarType
	 * @return
	 */
	public TextChunk newTextChunk(boolean doUnderlineArg, CharSequence untokenized, ScalarType scalarType)
	{
		return new TextChunk(doUnderlineArg, untokenized, scalarType);
	}
	/**
	 * @param copyChunk
	 */
	public TextChunk(TextChunk copyChunk)
	{
		super(copyChunk);
	}

	public TextToken newToken(String string, String delims, ParsedURL href)
	{
		return new TextToken(string, delims, href);
	}
	
	public TextToken newToken(TextToken prevToken)
	{
		return new TextToken(prevToken);
	}
	public TextToken newToken(String string, String delims, ParsedURL href, int style, int fontSize, int faceIndex) 
	{
		return new TextToken(string, delims, href, style, fontSize, faceIndex);
	}
	public static final ArrayList<TextToken>	EMPTY_COLLECTION							= new ArrayList<TextToken>(0);
	public static  Iterator<TextToken> 				EMPTY_ITERATOR								= EMPTY_COLLECTION.iterator();
	
	public Iterator<TextToken> iterator()
	{
		return tokens == null ? EMPTY_ITERATOR : tokens.iterator();
	}

}
