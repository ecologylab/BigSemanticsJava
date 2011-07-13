/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved. CONFIDENTIAL. Use is subject to
 * license terms.
 */
package ecologylab.semantics.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ecologylab.appframework.types.prefs.Pref;
import ecologylab.generic.StringBuilderPool;
import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.model.text.Term;
import ecologylab.semantics.model.text.TermDictionary;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.scalar.ScalarType;
import ecologylab.serialization.types.scalar.ScalarTypeInstanceConstants;
import ecologylab.textformat.NamedStyle;

/**
 * A text chunk from an HTML page; delimited by markup, or a size threshold; composed of an ordered
 * collection of {@link TextToken TextToken}s.
 */
abstract public @simpl_inherit
class TextChunkBase<T extends TextToken> extends ElementState implements
		ScalarTypeInstanceConstants,
		Iterable<T>
{
	
	@simpl_collection
	@simpl_scope(TextTokenTranslations.TEXT_TOKEN_SCOPE_NAME)
	@simpl_nowrap
	protected ArrayList<T>						tokens;
	/**
	 * Named Style for this text chunk. Default is to an anonymous style.
	 */
	@simpl_composite @xml_other_tags({"anon_style"})
	protected NamedStyle							namedStyle									= new NamedStyle(DEFAULT_POINT_SIZE);

	/**
	 * Current style name. Either this or anon style will be null so that only one will be sent to
	 * xml.
	 */
	@simpl_scalar
	protected String									styleName									= null;

	@simpl_scalar
	protected ParsedURL								commonHref								= null;

	@simpl_scalar
	protected float										nonStopIndex;

	private boolean										recycled;

	private ScalarType								scalarType								= null;

	
	public static final int						DEFAULT_POINT_SIZE				= 21;

	/** Estimate used for StringBuffer allocation. */
	public static int									CHARS_PER_TOKEN						= 12;

	public static final int						LEFT											= 0;

	public static final int						CENTER										= 1;

	public static final int						RIGHT											= 2;

	/**
	 * The maximum number of words for a text surrogate.
	 */
	public static final int				MAX_WORDS							= 9;

	/**
	 * The minimum number of words for a text surrogate.
	 */
	public static final int					MIN_WORDS							= 5;
	
	private static StringBuilderPool sbp = new StringBuilderPool(25);

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

	public abstract TextChunkBase<T> newTextChunk();

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
		boolean first		= true;

		while (matcher.find())
		{
			String group1 	= matcher.group(1);
			if (group1.contains("\n"))
				group1				= "\n";
			if (allowDelimitersInTokens && !first)
			{
				delimsBefore	= group1;
			}
			T textToken = newToken(matcher.group(2), delimsBefore, null);
			addTextToken(textToken);
			if (!allowDelimitersInTokens)
				delimsBefore	= scalarType.primaryDelimiter();
			first						= false;
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
				result += open + "a " + ecologylab.serialization.XMLTools.nameVal("href", href.url()) + close;
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
			recycled		= true;
			if (namedStyle != null)
				namedStyle.recycle();
			namedStyle 	= null;
			string 			= null;
			super.recycle();
			int last 		= size() - 1;
			for (int i = last; i >= 0; i--)
			{
				T tt = remove(i);
				tt.recycle();
			}
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

	/**
	 * Calculates the weight of a text chunk by summing all of the term weights contained in this
	 * chunk.
	 * 
	 * @param chunk
	 *          a chunk of text
	 * @return sum of weights of all terms in the specified chunk
	 */
	protected final float getWeight()
	{
		float weight = 0;
		for (int i = 0; i < this.size(); i++)
		{
			T cfTextToken = this.get(i);
			String tokenString = cfTextToken.string();
			if (tokenString.length() > 1)		//  ignore 1 character strings. they are not valuable.
			{
				Term xterm = cfTextToken.xterm();
				double w = xterm.idf();
				weight += w;
			}
		}
		return weight;
	}

	public final float getAvgWeight()
	{
		int size = size();
		if (size == 0)
		{
			return 0;
		}
		return getWeight() / size;
	}
	
	/**
	 * Returns a subchunk of a chunk where the subchunk contains the <code>begin</code>th word to the
	 * <code>end</code>th word.
	 * 
	 * @param chunk
	 *          a text chunk
	 * @param begin
	 *          the index in chunk of the first word to be in the subchunk
	 * @param end
	 *          the index in chunk of the last word to be in the subchunk
	 * @return a subchunk of the specified chunk
	 */
	public final TextChunkBase<T> getSubchunk(int begin, int end)
	{
		TextChunkBase<T> subchunk = this.newTextChunk();
		for (int i = begin; i <= end; i++)
		{
			if (!TermDictionary.mostObviousStopWordTerms.containsKey(removeNonAlpha(get(i).string()))
					|| !subchunk.isEmpty())
			{
				subchunk.add(get(i));
			}
		}
		int lastI = subchunk.size() - 1;
		while (lastI >= 0
				&& TermDictionary.mostObviousStopWordTerms.containsKey(removeNonAlpha(subchunk.get(lastI).string())))
		{
			subchunk.remove(lastI);
			lastI--;
		}
		return subchunk;
	}

	private static final String removeNonAlpha(String s)
	{
		StringBuilder buff = sbp.acquire();
		char c;
		for (int i = 0; i < s.length(); i++)
		{
			c = s.charAt(i);
			if (Character.isLetter(c))
				buff.append(Character.toLowerCase(c));
		}
		return sbp.releaseAndGetString(buff);
	}

	/**
	 * Retrieves the sentence from the context with the highest average term weight.
	 * 
	 * @param context
	 *          a text chunk
	 * @return sentence with the highest average term weight
	 */
	public final TextChunkBase<T> getSentence()
	{
		List<TextChunkBase<T>> sentences = new LinkedList<TextChunkBase<T>>();
		for (int i = 0; i < this.size();)
		{
			TextChunkBase<T> sentence = newTextChunk();
			int j = 0;
			boolean sentenceHasEmailAddr	= false;	// cleaned-up by andruid, jon, sashi 5/20/2010
			while ((i + j) < size())
			{
				T token = get(i + j);
				if (token.contains('@'))							// look out for email addresses. discard any sentence that seems to contain one.
					sentenceHasEmailAddr	= true;
				if (!sentenceHasEmailAddr)
					sentence.add(token);
				if (token.endsWithTerminal())
				{
					break;
				}
				j++;
			}
			i += j + 1;
			if (!sentenceHasEmailAddr && sentence.size() > 0)
			{
				sentences.add(sentence);
			}
		}

		int maxI = 0;
		if (sentences.size() == 0)
			return this;
		float maxVal = sentences.get(0).getAvgWeight();
		for (int i = 1; i < sentences.size(); i++)
		{
			float val = sentences.get(i).getAvgWeight();
			if (val > maxVal)
			{
				maxVal = val;
				maxI = i;
			}
		}
		TextChunkBase<T> sentence = sentences.get(maxI);
		return sentence;
	}

	/**
	 * Trim phatSurrogate for visualization part
	 * 
	 * @param semanticText
	 * 						Boolean to increase the maximum size of text surrogates
	 * 						set true for metaMetaData semantic action text, false for all else.
	 * @return	The skinny chunk, a short phrase from a larger context, which we may show to the user.
	 */
	public TextChunkBase<T> trimPhatChunk(boolean semanticText)
	{
		// Set text length max and min bound from prefs
		float modifier = Pref.lookupFloat("text_length_modifier", 1);
		int maxLength = Math.round(MAX_WORDS * modifier);
		int minLength = Math.round(MIN_WORDS * modifier);		
		
		// Find the sentence in the context with highest average weight
		TextChunkBase<T> sentence 	= this.getSentence();
		int sentenceSize 			= sentence.size();
		
		int sizeIncrease = (semanticText) ? 3 : 0;		
		
		if (sentenceSize > (maxLength + sizeIncrease))
		{
			// Shorten the sentence by examining all contiguous sub-sentences between MIN_WORDS and
			// MAX_WORDS lengths. The sub-sentence with the highest average value is the winner.
			TextChunkBase<T> maxChunk 	= null;
			float maxVal 					= Float.NEGATIVE_INFINITY;
			for (int i = 0; i <= (sentenceSize - minLength); i++)
			{
				for (int j = (minLength - 1); j < (maxLength + sizeIncrease); j++)
				{
					if ((i + j) >= sentenceSize)
					{
						break;
					}
					TextChunkBase<T> chunk = sentence.getSubchunk(i, i + j);
					float val = chunk.getAvgWeight();
					if (val > maxVal)
					{
						maxVal = val;
						maxChunk = chunk;
					}
				}
			}
			if (maxChunk != null && maxChunk.size() > 0)
			{
				maxChunk.get(0).setDelimsBefore("");
			}
			return maxChunk;
		}
		// Removes most obvious stop words off the front of a sentence
		sentence = sentence.getSubchunk(0, sentenceSize - 1);
		if (sentence.size() > 0)
		{
			sentence.get(0).setDelimsBefore("");
		}
		return sentence;
	}


}
