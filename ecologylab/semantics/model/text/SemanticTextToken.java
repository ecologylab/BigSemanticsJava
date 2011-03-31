package ecologylab.semantics.model.text;

import java.util.regex.Matcher;

import ecologylab.generic.StringTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.model.TextToken;
import ecologylab.serialization.simpl_inherit;

@simpl_inherit
public class SemanticTextToken extends TextToken
{
	protected Term term;
	
	public SemanticTextToken ()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public SemanticTextToken ( String s, ParsedURL h, int fontStyle, int tokenFontSize,
			String delims, int faceIndex, int under )
	{
		super(s, h, fontStyle, tokenFontSize, delims, faceIndex, under);
		// TODO Auto-generated constructor stub
	}

	public SemanticTextToken ( String s, String delims, ParsedURL h, int style, int fontSize,
			int faceIndex )
	{
		super(s, delims, h, style, fontSize, faceIndex);
		// TODO Auto-generated constructor stub
	}

	public SemanticTextToken ( String s, String delims, ParsedURL h )
	{
		super(s, delims, h);
		// TODO Auto-generated constructor stub
	}

	public SemanticTextToken ( TextToken previousToken )
	{
		super(previousToken);
		// TODO Auto-generated constructor stub
	}
	
	public static void noPunctuation(String input, StringBuilder termBuffy)
	{
		Matcher m = TermVector.WORD_REGEX.matcher(input);
		if (m.find())
		{
			int start = m.start();
			termBuffy.append(input, start, m.end());
			StringTools.toLowerCase(termBuffy);
		}
	}

	public Term term()
	{
		if (term == null)
		{
			StringBuilder termBuffy	= StringBuilderUtils.acquire();
			noPunctuation(string, termBuffy);
			if (termBuffy.length() > 0)
				term = TermDictionary.getTermForWord(termBuffy);
			else
				term	= TermDictionary.STOP_WORD;
			
			StringBuilderUtils.release(termBuffy);
		}
		return term;
	}

	public Term xterm()
	{
	   if (term == null)
		   term = TermDictionary.getTermForUnsafeWord(this.string());
	   return term;
	 }
	
}
