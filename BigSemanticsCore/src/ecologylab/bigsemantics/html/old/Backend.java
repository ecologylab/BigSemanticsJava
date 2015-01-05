/**
 * HTML Scanner/Parser
 * Hooks to HTMLPage for backend actions.
 * This could be abstracted out into an interface.
 *
 * Copyright 1996-9 by Andruid Kerne.  All rights reserved.
 */

package ecologylab.bigsemantics.html.old;

import java.util.HashMap;

/**
 * Parser backend interface.
 */
public interface Backend
{
/**
 * one of our special <code>meta</code> tags, name="CollageMachineNoUnderline"
 * use to indicate that hyperlinks should not be underlined when visualized
 */
   public void meta(HashMap<String, String> attributesMap);

/**
 * Parser found a body tag, w atrributes
 */
   public void newBody(HashMap<String, String> attributesMap);
/**
 * Parser found a <code>frame</code> tag, with a <code>src</code> attribute.
 */
   public void newFrame(HashMap<String, String> attributesMap);
/**
 * Parser found al <code>iframe</code> tag, with a <code>src</code> attribute.
 */
   public void newIFrame(HashMap<String, String> attributesMap);

/**
 * Parser got the start of a hyperlink, via <code>&lt;a href</code>
 */
   public void newAHref(HashMap<String, String> attributesMap);
   
   public void  newImageMapArea(HashMap<String, String> attributesMap);
   
   public void newMinedHref(String urlString);
/**
 * Parser got our container tag.
 */
   public void  setContainer(String urlString);
   
/**
 * Parser got the end of a hyperlink, via <code>&lt;/a&gt;</code>
 */
   public void closeHref();

/**
 * Parser found a single token of text -- outside of a tag.
 */
   public void newTextToken(String s, String delim);

   public void newWhitespace();
   
/**
 * Ends of paragraphs and tables.
 */
   public static final int STRONG_END	= 2;
/**
 * Ends of lines.
 */
   public static final int WEAKER_END	= 1;
/**
 * Ends of sentences.
 */
   public static final int WEAKEST_END	= 0;
   
/**
 * Starting a tage, like <code>&lt;tr&gt;</code>, that means that
 * other structures are automatically terminated.
 */
   public void closeBlock(String tag, HashMap<String, String> attributesMap);
   
   public void fontTag(HashMap<String, String> attributesMap);

/**
 * Start <code>title</code> tag. <code>TextToken</code>s until 
 * <code>&lt;/title&gt;</code> are part of the title, not regular text.
 */
   public void startTitle();
/**
 * <code>&lt;/title&gt;</code> tag.
 */
   public void closeTitle();

/**
 * Parser got <code>&lt;img src="..." alt="..."</code>
 */
   public void newImg(HashMap<String, String> attributesMap);

   public void newMinedImg(String urlString);
   
/**
 * Parser found a bold (or strong) tag or an end bold tag.
 */
   public void setBold(boolean on);
/**
 * Parser found an italic (or em) tag or an end italic tag.
 */
   public void setItalic(boolean on);
   
   public void streamClosed();

}
