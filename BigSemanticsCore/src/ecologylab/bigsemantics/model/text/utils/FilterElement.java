/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
 * CONFIDENTIAL. Use is subject to license terms.
 */
package ecologylab.bigsemantics.model.text.utils;

/**
 * Single pattern to be matched for {@link Filter filter}ing.
 */
public class FilterElement
{
   String	pattern;
   boolean	nonAlphaBefore;
   boolean	nonAlphaAfter;
   
   public FilterElement(String patternArg, boolean before, boolean after)
   {
      pattern		= patternArg;
      nonAlphaBefore	= before;
      nonAlphaAfter	= after;
   }
}

