/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved. CONFIDENTIAL. Use is subject to
 * license terms.
 */
package ecologylab.semantics.model;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import ecologylab.generic.StringBuilderPool;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.model.text.Term;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.Hint;
import ecologylab.serialization.simpl_inherit;

/**
 * Smallest unit of top-level text in an HTML page; that is, a token of text that lives outside of
 * html tags -- not a tag name, not an attribute, not part of a style, nor of javascript -- that
 * results from an html parse.
 * 
 * @author alexgrau
 */
public @simpl_inherit
class TextToken extends ElementState
{

	@simpl_scalar
	@simpl_hints(Hint.XML_LEAF)
	protected String											string												= "";

	/** Link for the TextToken, if one exists */
	@simpl_scalar
	protected ParsedURL										href;

	/** Delimiters that come before the string found in WordForms */
	@simpl_scalar
	protected String											delimsBefore									= " ";

	/**
	 * Style of the token, plus one. Style are found in Font. 0 is the same as the ChunkStyle, 1 is
	 * Plain, 2 is Bold, etc. Returned and set using values in Font, just plus or minus one. Done so
	 * that if the token is the same as the chunk, the value is not stored in the xml.
	 */
	@simpl_scalar
	protected int													stylePlusOne									= 0;

	/**
	 * Face index (font) for the token. Done in same manner as StylePlusOne. Uses same index as
	 * fontIndex just plus one so that 0 can be the same as the chunk, rather than -1. Although in the
	 * methods it is coded as -1.
	 */
	@simpl_scalar
	protected int													facePlusOne										= 0;

	/**
	 * Font size of the token. 0 means that it is the same as the Chunk.
	 */
	@simpl_scalar
	protected int													fontSize											= 0;

	// TODO Boolean
	@simpl_scalar
	@xml_other_tags({"eol"})
	protected int													endOfLine											= 0;

	/**
	 * Underline is an integer that has constants that mean different things. 0-Same as Chunk
	 * Underline Style. 1-Don't Underline Token 2-Do Underline String but not Delims 3-Do Underline
	 * Entire Token 4-Do Double Underline String but not Delims 5-Do Double Underline Entire Token
	 */
	@simpl_scalar
	protected int													underline											= 0;

	/** Variable used to denote the end of a link */
	public boolean												endOfLink;

	private boolean												fullStringIsOld								= true;

	private String												fullString;

	private long													ormId;

	public static final int	NOT_END_OF_LINE					= 0;

	public static final int	END_OF_LINE						= 1;

	/** Constant that indicates that the style or face index is the same as the chunk */
	public static final int	SAME_AS_CHUNK_STYLE_OR_FACE		= -1;

	/** Constant that indicates tha the font size is the same as the chunk */
	public static final int	SAME_AS_CHUNK_SIZE				= 0;

	/** Constant that indicates that you underline this token in the same fashion as the chunk */
	public static final int	SAME_AS_CHUNK_UNDERLINE			= 0;

	/** Constant that indicates that you do not underline this token */
	public static final int	UNDERLINE_NONE					= 1;

	/**
	 * Constant that indicates that you do underline the token string part of it but not the delims
	 * before.
	 */
	public static final int	UNDERLINE_STRING				= 2;

	/** Constant that indicates that you underline the entire token, even the delims before */
	public static final int	UNDERLINE_ENTIRE_TOKEN			= 3;

	/**
	 * Constant that indicates that you do double underline the token string part of it but not the
	 * delims before.
	 */
	public static final int	DOUBLE_UNDERLINE_STRING			= 4;

	/** Constant that indicates that you double underline the entire token, even the delims before */
	public static final int	DOUBLE_UNDERLINE_ENTIRE_TOKEN	= 5;
	
	public static final StringBuilderPool stringBufPool = new StringBuilderPool(25);

	/**
	 * Empty constructor for opening in xml translation.
	 */
	public TextToken ()
	{
		super();
	}

	/**
	 * Creates a token based on a string, delimeters and a link, uses the same other featurs as the
	 * chunk
	 * 
	 * @param s
	 * @param delims
	 * @param h
	 */
	public TextToken ( String s, String delims, ParsedURL h )
	{
		this(s, h, SAME_AS_CHUNK_STYLE_OR_FACE, SAME_AS_CHUNK_SIZE, delims,
				SAME_AS_CHUNK_STYLE_OR_FACE, SAME_AS_CHUNK_UNDERLINE);
	}

	/**
	 * Creates a textToken.
	 * 
	 * @param s
	 * @param delims
	 * @param h
	 * @param style
	 * @param fontSize
	 * @param faceIndex
	 */
	public TextToken ( String s, String delims, ParsedURL h, int style, int fontSize, int faceIndex )
	{
		this(s, h, style, fontSize, delims, faceIndex, SAME_AS_CHUNK_UNDERLINE);
	}

	/**
	 * This is for the coping previousToken
	 */
	public TextToken ( TextToken previousToken )
	{
		this(previousToken.string, previousToken.href, previousToken.fontStyle(), previousToken
				.getFontSize(), previousToken.delimsBefore, previousToken.faceIndex(), previousToken
				.getUnderline());
	}

	/**
	 * Creates a textToken with predesignated features.
	 * 
	 * @param s
	 * @param h
	 * @param fontStyle
	 * @param tokenFontSize
	 * @param delims
	 * @param faceIndex
	 * @param underlineOpp
	 */
	public TextToken ( String s, ParsedURL h, int fontStyle, int tokenFontSize, String delims,
			int faceIndex, int under )
	{
		if (s != null)
			string = s;
		href = h;
		stylePlusOne = fontStyle + 1;
		fontSize = tokenFontSize;
		delimsBefore = delims;
		facePlusOne = faceIndex + 1;
		underline = under;
	}

	public void setString ( String string )
	{
		if (string == null)
			this.string = "";
		else
			this.string = string;
		resetFullString();
	}
	
	public String lc()
	{
		return getString().toLowerCase();
	}

	public String toString ( )
	{
		StringBuilder sb = stringBufPool.acquire();
		sb.append(getString());
		sb.append("->");
		sb.append(href);
		return stringBufPool.releaseAndGetString(sb);
	}

	public String getString ( )
	{
		return string;
	}

	public String fullString ( )
	{
		if (fullStringIsOld)
			rebuildFullString();
		return fullString;
	}

	public int faceIndex ( )
	{
		return facePlusOne - 1;
	}

	public void setFaceIndex ( int faceIndex )
	{
		facePlusOne = faceIndex + 1;
	}

	public int getUnderline ( )
	{
		return underline;
	}

	public void setUnderline ( int value )
	{
		underline = value;
	}

	public boolean empty ( )
	{
		return string.equals("");
	}

	public ParsedURL getHref ( )
	{
		return href;
	}

	public int fontStyle ( )
	{
		return stylePlusOne - 1;
	}

	public int getStylePlusOne ( )
	{
		return stylePlusOne;
	}

	public int getFontSize ( )
	{
		return fontSize;
	}

	public String getDelimsBefore ( )
	{
		return delimsBefore;
	}

	public void setHref ( ParsedURL newHref )
	{
		href = newHref;
	}

	public void setHref ( String value )
	{
		if (!value.equals("null"))
		{
			try
			{
				// System.out.println("***********" + value);
				href = new ParsedURL(new URL(value));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
			href = null;
	}

	public void setFontStyle ( int style )
	{
		stylePlusOne = style + 1;
	}

	public void setStylePlusOne( int stylePlusOne )
	{
		this.stylePlusOne = stylePlusOne;
	}

	public void setFontSize ( int tokenSize )
	{
		fontSize = tokenSize;
	}

	public void setDelimsBefore ( String s )
	{
		delimsBefore = s;
		resetFullString();
	}

	public void addDelimBefore ( String s )
	{
		StringBuilder sb = stringBufPool.acquire();
		sb.append(delimsBefore);
		sb.append(s);
		delimsBefore = stringBufPool.releaseAndGetString(sb);
		resetFullString();
	}

	public void addDelimBefore ( String s, int i )
	{
		StringBuilder sb = stringBufPool.acquire();
		sb.append(delimsBefore);
		sb.insert(i+1, s);
		delimsBefore = stringBufPool.releaseAndGetString(sb);
		resetFullString();
	}

	public void removeDelimBefore ( )
	{
		if (!delimsBefore.equals(""))
		{
			delimsBefore = delimsBefore.substring(0, delimsBefore.length() - 1);
			resetFullString();
		}
	}

	public void removeDelimBefore ( int i )
	{
		if (!delimsBefore.equals(""))
		{
			StringBuilder sb = stringBufPool.acquire();
			sb.append(delimsBefore);
			sb.deleteCharAt(i);
			delimsBefore = stringBufPool.releaseAndGetString(sb);
			resetFullString();
		}
	}

	/**
	 * Free resources associated with this.
	 */
	public void recycle ( )
	{
		href = null;
		super.recycle();
	}

	public int getEndOfLine ( )
	{
		return endOfLine;
	}

	public void setEndOfLine ( int endOfLine )
	{
		this.endOfLine = endOfLine;
	}
	
	private void resetFullString()
	{
		fullStringIsOld = true;
	}
	
	private void rebuildFullString()
	{
		StringBuilder sb = stringBufPool.acquire();
		sb.append(delimsBefore);
		sb.append(getString());
		fullString = stringBufPool.releaseAndGetString(sb);
		fullStringIsOld = false;
	}
	
	/**
	 * 
	 * @param c
	 * @return	true if this token has a String, and the character is contained in it.
	 */
	public boolean contains(char c)
	{
		return string == null ? false : string.indexOf(c) >= 0;
	}
	
	public Term xterm()
	{
		return null;
	}
	
	public static final Set<String>	TERMINAL_PUNCTUATION	= new HashSet<String>();
	static
	{
		TERMINAL_PUNCTUATION.add(".");
		TERMINAL_PUNCTUATION.add(".\"");
		TERMINAL_PUNCTUATION.add("?");
		TERMINAL_PUNCTUATION.add("?\"");
		TERMINAL_PUNCTUATION.add("!");
	}

	public static final Set<String>	TERMINAL_EXCEPTIONS		= new HashSet<String>();
	static
	{
		TERMINAL_EXCEPTIONS.add("i.e.");
		TERMINAL_EXCEPTIONS.add("e.g.");
		TERMINAL_EXCEPTIONS.add("mr.");
		TERMINAL_EXCEPTIONS.add("mrs.");
		TERMINAL_EXCEPTIONS.add("ms.");
		TERMINAL_EXCEPTIONS.add("jr.");
		TERMINAL_EXCEPTIONS.add("sr.");
		TERMINAL_EXCEPTIONS.add("sgt.");
		TERMINAL_EXCEPTIONS.add("u.s.");
		TERMINAL_EXCEPTIONS.add("corp.");
		TERMINAL_EXCEPTIONS.add("inc.");
		TERMINAL_EXCEPTIONS.add("co.");
		TERMINAL_EXCEPTIONS.add("jan.");
		TERMINAL_EXCEPTIONS.add("feb.");
		TERMINAL_EXCEPTIONS.add("mar.");
		TERMINAL_EXCEPTIONS.add("apr.");
		// No need for May
		TERMINAL_EXCEPTIONS.add("jun.");
		TERMINAL_EXCEPTIONS.add("jul.");
		TERMINAL_EXCEPTIONS.add("aug.");
		TERMINAL_EXCEPTIONS.add("sep.");
		TERMINAL_EXCEPTIONS.add("oct.");
		TERMINAL_EXCEPTIONS.add("nov.");
		TERMINAL_EXCEPTIONS.add("dec.");
	}
	
	protected static Pattern	HONORIFIC_MATCHER	= Pattern.compile("(\\p{Upper}\\.)+");

	public boolean endsWithTerminal()
	{
		String string = this.getString();
		if (!TERMINAL_EXCEPTIONS.contains(string.toLowerCase())
				&& !HONORIFIC_MATCHER.matcher(string).matches())
		{
			return endsWith(TERMINAL_PUNCTUATION);
		}
		return false;
	}

	private boolean endsWith(java.util.Collection<String> endings)
	{
		String string = this.getString();
		for (String ending : endings)
		{
			if (string.endsWith(ending))
			{
				return true;
			}
		}
		return false;
	}

	public long getOrmId()
	{
		return ormId;
	}

	public void setOrmId(long ormId)
	{
		this.ormId = ormId;
	}

	public int getFacePlusOne()
	{
		return facePlusOne;
	}

	public void setFacePlusOne(int facePlusOne)
	{
		this.facePlusOne = facePlusOne;
	}

}
