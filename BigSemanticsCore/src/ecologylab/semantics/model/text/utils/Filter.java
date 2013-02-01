/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved. CONFIDENTIAL. Use is subject to
 * license terms.
 */
package ecologylab.semantics.model.text.utils;

import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;

/**
 * Filters out <code>URL</code>s that seem to be from ad servers.
 */
public class Filter extends Debug
{
	boolean								active			= true;

	static FilterElement	patterns[]	=
	{
		// new FilterElement("advertiser", false, false),
		new FilterElement("ads", true, false), new FilterElement("adv", true, false),
		new FilterElement("ad", true, true),
		new FilterElement("adx", true, true),
		new FilterElement("doubleclick", true, true),
		// new FilterElement("help", false, false),
		new FilterElement("banner", false, false), new FilterElement("wp-srv", false, false),
		new FilterElement("creditcard", false, false), new FilterElement("promos", true, false),
		new FilterElement("click", false, false), new FilterElement("shopping", false, false), // MUST
		// BE
		// LAST
	};

	public static int			count				= patterns.length;

	public static void shoppingOK()
	{
		if (count == patterns.length)
			count--;
	}

	/*
	 * public boolean match(URL url) { return match(StringTools.noAnchorNoQueryPageString(url)); }
	 */
	/* The parameter of this method has been changed from URL to ParsedURL */
	public boolean match(ParsedURL parsedURL)
	{
		/* lc() method returns lower case url string from the ParsedURL */
		return matchLc(parsedURL.lc());
	}

	public boolean match(String s)
	{
		return matchLc(s.toLowerCase());
	}

	public boolean matchLc(String s)
	{
		if (!active)
			return false;
		boolean result = false;
		int numPatterns = count;
		String save = s;
		for (int i = 0; i != numPatterns; i++)
		{
			s = save;
			FilterElement thisFilter = patterns[i];
			String pattern = thisFilter.pattern;
			int patternLength = pattern.length();
			boolean nonAlphaBefore = thisFilter.nonAlphaBefore;
			boolean nonAlphaAfter = thisFilter.nonAlphaAfter;

			for (int index = s.indexOf(pattern); index != -1; index = s.indexOf(pattern))
			{
				result = true;
				if (nonAlphaBefore)
				{
					int before = index - 1;
					if (before >= 0)
						if (Character.isJavaIdentifierStart(s.charAt(before)))
							result = false;
					// System.out.println("\tChecking before="+s.charAt(before)+
					// "... " + result);
				}
				if (nonAlphaAfter)
				{
					int sLength = s.length();
					int after = index + patternLength;
					if (after < sLength)
						if (Character.isJavaIdentifierStart(s.charAt(after)))
							result = false;
				}
				if (result)
					break;
				s = s.substring(index + patternLength);
			}
			if (result)
				break;
		}
		return result;
	}

	public static void main(String s[])
	{
		Filter f = new Filter();
		for (int i = 0; i != s.length; i++)
		{
			String x = s[i];
			println(f.match(x) + " " + x);
		}
	}
}
