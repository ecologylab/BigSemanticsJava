/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved. CONFIDENTIAL. Use is subject to
 * license terms.
 */
package ecologylab.semantics.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.textformat.NamedStyle;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldDescriptor;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_nested;
import ecologylab.xml.ElementState.xml_other_tags;
import ecologylab.xml.types.scalar.ScalarType;
import ecologylab.xml.types.scalar.ScalarTypeInstanceConstants;

/**
 * A text chunk from an HTML page; delimited by markup, or a size threshold; composed of an ordered
 * collection of {@link TextToken TextToken}s.
 */
abstract public @xml_inherit
class TextChunkBase<T extends TextToken> extends ElementState implements
		ScalarTypeInstanceConstants,
		Iterable<T>
{
	
	@xml_collection
	@xml_scope(TextTokenTranslations.TEXT_TOKEN_SCOPE_NAME)
	@xml_nowrap
	protected ArrayList<T>						tokens;
	/**
	 * Named Style for this text chunk. Default is to an anonymous style.
	 */
	@xml_nested @xml_other_tags({"anon_style"})
	protected NamedStyle							namedStyle									= new NamedStyle(DEFAULT_POINT_SIZE);

	/**
	 * Current style name. Either this or anon style will be null so that only one will be sent to
	 * xml.
	 */
	@xml_attribute
	protected String									styleName									= null;

	@xml_attribute
	protected ParsedURL								commonHref								= null;

	@xml_attribute
	protected float										nonStopIndex;

	private boolean										recycled;

	private ScalarType								scalarType								= null;

	
	public static final int						DEFAULT_POINT_SIZE				= 21;

	/** Estimate used for StringBuffer allocation. */
	public static int									CHARS_PER_TOKEN						= 12;

	public static final int						LEFT											= 0;

	public static final int						CENTER										= 1;

	public static final int						RIGHT											= 2;

	static HashMap<String, String[]>	garbageFilterMap					= new HashMap<String, String[]>();

	final static String								garbageFilterStrings[][]	=
																															{
																															{ "all", "rights", "reserved" },
																															{ "is", "a", "trademark" },
																															{ "copyright" },
																															{ "last", "updated" },
																															{ "you", "searched" },
																															{ "email", "inquiries" },
																															{ "best", "viewed", "with" },
																															{ "search", "for", "more" },
																															{ "password" },
																															{ "last", "modified" },
																															{ "posted", "at" },
																															{ "subscriber", "id" },
																															{ "terms", "under" },
																															{ "text", "only" },
																															{ "text", "version" },
																															{ "search", "results" },
																															{ "hotbot", "results" },
																															{ "disclaimer" },
																															{ "see", "results", "from" },
																															{ "altavista", "found" },
																															{ "results", "for" },
																															{ "hits", "since" },
																															{ "visitor", "number" },
																															{ "error", "occurred" },
																															{ "support", "frames" },
																															{ "error", "404" },
																															{ "found", "error" },
																															{ "contact", "us" },
																															{ "slide", "shows" },
																															{ "see", "sample" },
																															{ "special", "offer" },
																															{ "privacy", "policy" },
																															{ "license", "agreement" },
																															{ "terms", "of", "use" },
																															{ "sign", "up" },
																															{ "sign", "in" },
																															{ "sign", "off" },
																															{ "ameritrade" }, };

	static
	{
		for (int i = 0; i != garbageFilterStrings.length; i++)
		{
			String[] thatFilter = garbageFilterStrings[i];
			garbageFilterMap.put(thatFilter[0], thatFilter);
		}
	}

	/**
	 * Empty constructor for opening in xml translation.
	 */
	protected TextChunkBase()
	{
		this(false);
	}

	/**
	 * A text chunk from an HTML page; delimited by markup, or a size threshold; composed of an
	 * ordered collection of {@link TextToken TextToken}s.
	 */
	protected TextChunkBase(boolean doUnderline, ParsedURL commonHref)
	{
		this(doUnderline);
		this.commonHref = commonHref;
	}

	/**
	 * A text chunk from an HTML page; delimited by markup, or a size threshold; composed of an
	 * ordered collection of {@link TextToken TextToken}s.
	 */
	protected TextChunkBase(boolean doUnderline, ParsedURL commonHref, int size, int faceIndex,
			int fontStyle, int alignment)
	{
		this(doUnderline);
		this.commonHref = commonHref;
		namedStyle().setFontSize(size);
		namedStyle().setFaceIndex(faceIndex);
		namedStyle().setFontStyle(fontStyle);
		namedStyle().setAlignment(alignment);
	}

	/**
	 * A text chunk from an HTML page; delimited by markup, or a size threshold; composed of an
	 * ordered collection of {@link TextToken TextToken}s.
	 */
	protected TextChunkBase(boolean doUnderline)
	{
		this(doUnderline, STRING_SCALAR_TYPE);
	}

	/**
	 * A text chunk from an HTML page; delimited by markup, or a size threshold; composed of an
	 * ordered collection of {@link TextToken TextToken}s.
	 */
	protected TextChunkBase(boolean doUnderline, ScalarType scalarType)
	{
		namedStyle().setUnderline(doUnderline);
		this.scalarType = scalarType;
		this.tokens = new ArrayList<T>();
	}

	/**
	 * A text chunk from an HTML page; delimited by markup, or a size threshold; composed of an
	 * ordered collection of {@link TextToken TextToken}s.
	 */
	protected TextChunkBase(boolean doUnderlineArg, CharSequence untokenized)
	{
		this(doUnderlineArg);
		tokenize(untokenized, scalarType);
	}

	/**
	 * @param doUnderlineArg
	 * @param untokenized
	 * @param delims
	 * @param keepDelims
	 *          true if the delimiters should be kept in the String values of each token.
	 */
	protected TextChunkBase(boolean doUnderlineArg, CharSequence untokenized, ScalarType scalarType)
	{
		this(doUnderlineArg, scalarType);
		tokenize(untokenized, scalarType);
	}

	/**
	 * A text chunk from an HTML page; delimited by markup, or a size threshold; composed of an
	 * ordered collection of {@link TextToken TextToken}s.
	 */
	protected TextChunkBase(TextChunkBase<T> copyChunk)
	{
		this(copyChunk.namedStyle.underline());
		int size = copyChunk.size();
		for (int i = 0; i < size; i++)
		{
			T newToken = newToken(copyChunk.token(i));
			add(newToken);
		}
	}

	/**
	 * Factory method to call the correct constructor.
	 * 
	 * @param doUnderlineArg
	 * @param untokenized
	 * @param scalarType
	 * @return
	 */
	public abstract TextChunkBase<T> newTextChunk(boolean doUnderlineArg, CharSequence untokenized,
			ScalarType scalarType);

	/**
	 * Create a new constituent TextToken of the correct subtype.
	 * 
	 * @param string
	 * @param href
	 * @return
	 */
	abstract public T newToken(String string, String delims, ParsedURL href);

	/**
	 * Create a new constituent TextToken of the correct subtype.
	 * 
	 * @param string
	 * @param delmis
	 * @param href
	 * @param style
	 * @return
	 */
	abstract public T newToken(String string, String delims, ParsedURL href, int style, int fontSize,
			int faceIndex);

	/**
	 * Create a new constituent TextToken of the correct subtype.
	 * 
	 * @param prevToken
	 * @return
	 */
	abstract public T newToken(TextToken prevToken);

	/**
	 * Tokenizes a string given a ScalarType
	 * 
	 * @param untokenized
	 * @param scalarType
	 */
	protected void tokenize(CharSequence untokenized, ScalarType scalarType)
	{
		if (scalarType == null)
			error("tokenize() scalarType==null; untokenized = " + untokenized);

		Pattern pattern = scalarType.delimitersTokenizer();
		Matcher matcher = pattern.matcher(untokenized);

		String delimsBefore = "";
		final boolean allowDelimitersInTokens = scalarType.allowDelimitersInTokens();
		while (matcher.find())
		{
			if (allowDelimitersInTokens)
				delimsBefore = matcher.group(1);
			T textToken = newToken(matcher.group(2), delimsBefore, null);
			addTextToken(textToken);
			if (!allowDelimitersInTokens)
				delimsBefore = scalarType.primaryDelimiter();
		}
	}

	public void initDoubleUnderline()
	{
		int hrefInRow = 0;
		for (int i = 0; i < this.size(); i++)
		{
			T token = this.get(i);
			if (token.href() != null)
			{
				if (hrefInRow == 0)
				{
					token.setUnderline(TextToken.DOUBLE_UNDERLINE_STRING);
				}
				else
				{
					token.setUnderline(TextToken.DOUBLE_UNDERLINE_ENTIRE_TOKEN);
				}
				hrefInRow++;
			}
			else
			{
				hrefInRow = 0;
			}
		}
	}

	/**
	 * Adds and Tokenizes a string to the end of the TextChunkBase
	 * 
	 * @param string
	 */
	public void appendString(CharSequence string)
	{
		tokenize(string, scalarType);
	}

	/**
	 * Adds the tokens in TextChunkBase to the end of this TextChunkBase
	 * 
	 * @param textChunk
	 */
	public void appendTextChunk(TextChunkBase<T> textChunk)
	{
		for (T textToken : textChunk)
		{
			addTextToken(textToken);
		}
	}
	
	

	/** Should be called addTextToken(). */
	public void add(String string, ParsedURL href)
	{
		T token;
		token = newToken(string, "", href);
		add(token);
	}

	public void add(String string, String delims, ParsedURL href)
	{
		T token;
		token = newToken(string, delims, href);
		add(token);
	}

	public void addTextToken(T textToken)
	{
		if (textToken == null)
		{
			return;
		}
		add(textToken);
	}

	public void endLink()
	{
		((TextToken) lastElement()).endOfLink = true;
	}

	/**
	 * @return the length in characters of the TextChunk does include whitespace
	 */
	public int length()
	{
		int sum = 0;
		int n = size();

		for (int i = 0; i != n; i++)
		{
			sum += token(i).fullString().length();
		}

		return sum;
	}

	/**
	 * Returns the ith TextToken
	 * 
	 * @param i
	 * @return
	 */
	public T token(int i)
	{
		return (i >= size()) ? null : get(i);
	}

	/**
	 * Returns the ith WordForm.
	 * 
	 * @param i
	 * @return
	 */
	public TextToken textToken(int i)
	{
		return token(i);
	}

	/**
	 * Returns the string for the token i in the chunk.
	 * 
	 * @param i
	 * @return
	 */
	public String string(int i)
	{
		TextToken token = token(i);
		return (token == null) ? null : token.fullString();
	}

	public String lc(int i)
	{
		TextToken token = token(i);
		return (token == null) ? null : token.lc();
	}

	public String string()
	{
		return stringBuilder().toString();
	}

	public StringBuilder stringBuilder()
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i != size(); i++)
		{
			TextToken t = token(i);
			sb.append(t.delimsBefore());
			sb.append(t.string());
		}
		return sb;
	}

	public boolean empty()
	{
		// return (size() == 0) || token(0).empty();
		return (size() == 0 || string().trim().equals(""));
	}

	static final String	xmlCloseAnchor			= "&lt;/a&gt;";

	static final String	regularCloseAnchor	= "</a>";

	/**
	 * The equivalent of emitHtml(false)
	 * 
	 * @return An HTML representation of this TextChunkBase.
	 */
	public String emitHtml()
	{
		return emitHtml(false);
	}

	/**
	 * @param xmlBrackets
	 *          If true, generate HTML for inclusion within XML.
	 * @return An HTML representation of this TextChunkBase.
	 */
	public String emitHtml(boolean xmlBrackets)
	{
		String result = "";
		boolean inHref = false;
		String open, close, closeAnchor;

		if (xmlBrackets)
		{
			open = "&lt;";
			close = "&gt;";
			closeAnchor = xmlCloseAnchor;
		}
		else
		{
			open = "<";
			close = ">";
			closeAnchor = regularCloseAnchor;
		}

		for (int i = 0; i != size(); i++)
		{
			TextToken thatToken = token(i);
			ParsedURL href = thatToken.href();

			if (!inHref && (href != null))
			{
				result += open + "a " + ecologylab.xml.XMLTools.nameVal("href", href.url()) + close;
				inHref = true;
			}

			result += thatToken.delimsBefore() + thatToken.string() + " ";

			if (inHref && (href == null))
			{
				result += closeAnchor;
				inHref = false;
			}
		}

		if (inHref)
		{
			result += closeAnchor;
		}

		return result;
	}

	public T lastElement()
	{
		return (size() == 0) ? null : this.get(size() - 1);
	}

	public void removeElementAt(int i)
	{
		if (tokens != null)
			tokens.remove(i);
	}

	/**
	 * @return true if this TextChunkBase looks like garbage
	 */
	public boolean isGarbage()
	{
		String s0;
		// prime the pump
		String s1 = lc(0);
		String s2 = lc(1);

		int n = size();

		for (int i = 0; i < n; i++)
		{
			// lower case w all lower case in contentIntegrator filter -- ignore case!m
			s0 = s1;
			s1 = s2;
			s2 = lc(i + 2);
			String filterJ[] = garbageFilterMap.get(s0);

			if (filterJ != null)
			{
				int filterLength = filterJ.length;

				if ((filterLength < 2)
						|| (StringTools.contains(s1, filterJ[1]) && ((filterLength < 3) || StringTools
								.contains(s2, filterJ[2]))))
				{
					return true;
				}
			}
		}
		return false;
	}

	String	string;

	// TODO -- invaliate this cached string when editing happens!!!!!!

	public String toString()
	{
		StringBuilder buffy	= toStringBuilder();

		String result = StringTools.toString(buffy);
		
		StringBuilderUtils.release(buffy);
		
		return result;
	}

	/**
	 * @param buffy
	 */
	public void toStringBuilder(StringBuilder buffy)
	{
		int size = size();
		for (int i = 0; i < size; i++)
		{
			buffy.append(token(i).fullString());
		}
	}
	
	public StringBuilder toStringBuilder()
	{
		StringBuilder result	= StringBuilderUtils.acquire();
		toStringBuilder(result);
		return result;
	}	

	public ParsedURL commonHref()
	{
		return commonHref;
	}

	/**
	 * An HTML representation of this MediaElement, suitable for drag and drop, copy and paste, ....
	 * 
	 * @return HTML String.
	 */
	public String toHTML()
	{
		int size = size();
		StringBuffer buffy = new StringBuffer(size * CHARS_PER_TOKEN);
		ParsedURL currentPURL = null;
		for (int i = 0; i < size; i++)
		{
			TextToken token = token(i);
			ParsedURL tokenPURL = token.href();
			if (currentPURL != tokenPURL)
			{
				// changes in hyperlink
				if (currentPURL != null)
				{
					// close a hyperlink
					buffy.append("</a>");
				}
				if (tokenPURL != null)
				{
					// open a hyperlink
					buffy.append("<a href=\"").append(tokenPURL.toString()).append("\">");
				}
			}
			buffy.append(token.fullString());
		}

		// close hyperlink if still open
		if (currentPURL != null)
		{
			buffy.append("</a>");
		}

		return buffy.toString();
	}

	public ScalarType scalarType()
	{
		return scalarType;
	}

	/**
	 * Clear data structures and references to enable garbage collecting of resources associated with
	 * this.
	 */
	public void recycle()
	{
		if (!recycled)
		{
			if (namedStyle != null)
				namedStyle.recycle();
			namedStyle = null;
			recycled = true;
			string = null;
			super.recycle();
		}
	}

	public NamedStyle namedStyle()
	{
		return namedStyle;
	}

	public void setNamedStyle(NamedStyle style)
	{
		this.namedStyle = style;
	}

	public String styleName()
	{
		return styleName;
	}

	public void setStyleName(String styleName)
	{
		this.styleName = styleName;
	}

	static final String	TEST_STRING	= "Querying Web Metadata: Native Score\nManagement and Text Support\nin Databases\nG\n¨\nULTEKIN\n¨\nOZSOYO\n?\nGLU\nCase Western Reserve University\nISMAIL SENG\n¨\nOR ALTING\n¨\nOVDE\nBilkent Universit";

	
	public T get(int i)
	{
		return tokens == null ? null : tokens.get(i);
	}
	
	public int size()
	{
		return tokens == null ? 0 : tokens.size();
	}
	
	public boolean add(T token)
	{
		return (tokens == null) ? false :	tokens.add(token);
	}
	
	public void add(int index, T token)
	{
		if (tokens != null) 
			tokens.add(index, token);
	}
	
	abstract public Iterator<T> iterator();
	
	public T remove(int i)
	{
		return tokens == null ? null : tokens.remove(i);
	}
	
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	public void clear()
	{
		if (tokens != null)
			tokens.clear();
	}
}
