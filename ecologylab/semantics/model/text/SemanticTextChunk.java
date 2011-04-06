/**
 * 
 */
package ecologylab.semantics.model.text;

import java.util.ArrayList;
import java.util.Iterator;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.model.TextChunkBase;
import ecologylab.semantics.model.TextToken;
import ecologylab.serialization.types.scalar.ScalarType;

/**
 * @author andruid
 *
 */
public class SemanticTextChunk extends TextChunkBase<SemanticTextToken>
{

	/**
	 * @param doUnderline
	 * @param commonHref
	 */
	public SemanticTextChunk(boolean doUnderline, ParsedURL commonHref)
	{
		super(doUnderline, commonHref);
	}

	/**
	 * @param doUnderline
	 */
	public SemanticTextChunk(boolean doUnderline)
	{
		super(doUnderline);
	}

	/**
	 * @param doUnderline
	 * @param scalarType
	 */
	public SemanticTextChunk(boolean doUnderline, ScalarType scalarType)
	{
		super(doUnderline, scalarType);
	}

	public SemanticTextChunk()
	{
		super();
	}
	/**
	 * @param copyChunk
	 */
	public SemanticTextChunk(SemanticTextChunk copyChunk)
	{
		super(copyChunk);
	}


	/**
	 * @param doUnderlineArg
	 * @param untokenized
	 */
	public SemanticTextChunk(boolean doUnderlineArg, CharSequence untokenized)
	{
		super(doUnderlineArg, untokenized);
	}

	public SemanticTextChunk(CharSequence untokenized)
	{
		this(false, untokenized);
	}
	/**
	 * @param doUnderlineArg
	 * @param untokenized
	 * @param scalarType
	 */
	public SemanticTextChunk(boolean doUnderlineArg, CharSequence untokenized,
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
	@Override
	public SemanticTextChunk newTextChunk(boolean doUnderlineArg, CharSequence untokenized, ScalarType scalarType)
	{
		return new SemanticTextChunk(doUnderlineArg, untokenized, scalarType);
	}
	@Override
	public SemanticTextToken newToken(String string, String delims, ParsedURL href)
	{
		return new SemanticTextToken(string, delims, href);
	}
	
	@Override

	public SemanticTextToken newToken(String string, String delims, ParsedURL href, int style, int fontSize, int faceIndex) 
	{
		return new SemanticTextToken(string, delims, href, style, fontSize, faceIndex);
	}

	@Override
	public SemanticTextToken newToken ( TextToken prevToken )
	{
		return new SemanticTextToken(prevToken);
	}
	public static final ArrayList<SemanticTextToken>	EMPTY_COLLECTION							= new ArrayList<SemanticTextToken>(0);
	public static  Iterator<SemanticTextToken> 				EMPTY_ITERATOR								= EMPTY_COLLECTION.iterator();
	
	@Override
	public Iterator<SemanticTextToken> iterator()
	{
		return tokens == null ? EMPTY_ITERATOR : tokens.iterator();
	}

	@Override
	public TextChunkBase<SemanticTextToken> newTextChunk()
	{
		return new SemanticTextChunk();
	}
}
