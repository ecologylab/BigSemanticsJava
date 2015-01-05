/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
 * CONFIDENTIAL. Use is subject to license terms.
 */
package ecologylab.bigsemantics.html.old;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import ecologylab.generic.Debug;
import ecologylab.generic.StringBuilderPool;
import ecologylab.generic.StringTools;

/**
 * Hand written HTML scanner; provides tokens to {@link Parser the Parser}.
 */
public class Scan
extends Debug
implements ecologylab.serialization.CharacterConstants
{
   public String	urlString; // just convenient for debug messages
   
   ////////////// class wide //////////////
   BufferedReader		bufferedReader;
   
   /**
	* A *very* efficient lookup table for whitespace characters.
	*/
   static boolean	whitespaceChars[];
   
   static final char SPACE	= ' '; // 0x20
   
   static
   {
      whitespaceChars	= new boolean[SPACE + 1];
      whitespaceChars['\n']	= true;	// 0x0a
      whitespaceChars['\t']	= true;	// 0x09
      whitespaceChars['\f']	= true;	// 0x0c	 form feed
      whitespaceChars['\r']	= true;	// 0x0d
      whitespaceChars[' ']	= true;	// 0x20
   }
   
   //+++++++++++++++++ Scanner State ++++++++++++++++++++//
   int		mode;
   // boolean	ignoreMode; // i added & removed this 2/05 -- andruid
   
   static public final int BUFFER_SIZE	= 512;
   //StringBuffer buffer	= new StringBuffer(BUFFER_SIZE);

   static StringBuilderPool stringBuffersPool = 
	   new StringBuilderPool(16, 16, BUFFER_SIZE);
   
   StringBuilder	buffer  	= stringBuffersPool.nextBuffer();
   
   /**
	* the actual token that was returned by scan().
	*/ 	
   public String	sval;
   String	pushBackBuffer;
   int	  	pushBackVal;

   /**
	* char that delimited the current/last quoted value
	*/
   char		quoteChar	= '"';
   
   /**
	* Looking for entities (triggered by &), tags (triggered by start
	* tag), words, whitespace.
	*/
   static final int	OUTSIDE_TAG_MODE	= 0;
   
   /**
	* Looking for quoted vals, unquoted attr_vals, =, tag close.
	*/
   static final int	INSIDE_TAG_MODE		= 1;
   
   /**
	* triggered by = inside a tag
	*/
   static final int	VAL_MODE		= 2;
   
   /**
	* Looking for quoted vals, unquoted attr_vals, =, tag close.
	*/
   static final int	QUOTED_VAL_MODE		= 3;
   
   /**
	* Looking for end comment.
	*/
   static final int	COMMENT_MODE		= 4;
   
   
   /**
    * looking for comments in the javascript
    */
   static final int SCRIPT_COMMENT_MODE	= 5;

   // from StreamTokenizer
   /**
	* scan() return value for end of file.
	*/
   public static final int TT_EOF		= -1;
   
   // i think that 0ax = 10 decimal
   // public static final int TT_EOL		= '\n';
   // public static final int TT_NUMBER		= -2;
   /**
	* scan() return value for a word of text.
	*/
   public static final int TT_WORD		= -3;
   
   /**
	* scan() return value for a word of text.
	*/
   public static final int TT_UNKNOWN		= -4;
   
   /**
	* scan() return value for whitespace between tokens.
	*/
   public static final int TT_WHITESPACE	= -5;
   
   /**
	* scan() return value for the start of an HTML comment.
	*/
   public static final int TT_COMMENT		= -8;
   
   /**
	* scan() return value for end of an HTML comment.
	*/
   public static final int TT_END_COMMENT	= -9;
   
   /**
	* scan() return value for an HTML tag (an xml element). The actual tag is in sval.
	*/
   public static final int TT_TAG		= -10;

   // ??? or do we just return '>' =  62
   /**
	* scan() return value for end of an HTML tag/element.
	*/
   public static final int TT_TAG_CLOSE		= -11;
   
   /**
	* scan() return value for a named or numbered entity.
	*/
   public static final int TT_ENTITY		= -21;
   
   /**
	* scan() return value for the name of an HTML/XML attribute.
	*/
   public static final int TT_ATTR		= -22;
   
   /**
	* scan() return value for the value of an HTML/XML attribute that was specified without quotes.
	*/
   public static final int TT_UNQUOTED_VAL	= -23;
   
   /**
	* scan() return value for the value of an HTML/XML attribute that was specified inside quotes.
	*/
   public static final int TT_QUOTED_VAL	= -24;

   /**
	* Try a 64k buffer.
	*/
   static final int	READER_BUFFER_SIZE	= 1024 * 64;

   public Scan(InputStream stream)
   {
      //bufferedStream	= new BufferedInputStream(streamArg, BUFFER_SIZE)
      try
			{
				bufferedReader = new BufferedReader(new InputStreamReader(stream, "unicode"));
			}
			catch (UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				error("uh oh! unsupported enconding. this means dnd is broken :(");
				e.printStackTrace();
			}
   }
   
   char	prevChar;
   boolean usePrev = false;

   /**
    * Scan the input for a token. Ignores characters less than 0x20 other than TAB, CR, and LF.
    * @return	a code that indicates the kind of token that was found.
    * 			the token itself is returned in the variable sval.
    */
   
   public int scan()
      throws IOException
	  {
		 int val		= TT_UNKNOWN;
		 int count		= ((mode == COMMENT_MODE) && sval.endsWith("--"))
			? 2 : 0;
		 boolean todo	= true;
		 boolean reset	= false;
		 
		 boolean breakTerm = false;

		 // !!!this is totally implementation dependent!!!
		 // it forces the buffer to be copied, and resized to
		 //      buffer.setLength(buffer.length() + 1); // force copy
		 StringTools.clear(buffer);

		 char c	 = (char) -1;
		 int dashes	= 0;
		 
		 while (todo)
		 {
			if (usePrev)
			{
			   c = prevChar;
			   usePrev = false;
			}
			else
			{
			   int ic = bufferedReader.read();
			   if (ic == -1)
			   {
				  val = TT_EOF;
				  break;
			   }
			   c = (char) ic;
			}
			if (c < 0x20)
			   switch (c)
			   {
			   case TAB:
			   case CR:
			   case LF:
				  break;
			   default:	// ignore other control characters because the XML spec doesnt allow them
				  // (), and we wont do anything useful with them, anyway.
				  return scan();
			   }

			count++;
			switch (mode)
			{
			case OUTSIDE_TAG_MODE:
			   // whitespace is significant
			   // entity is significant
			   // looking for  whitespace, entity, word, tag
			   // tag will cause transition -> INSIDE_TAG_MODE
			   if ((c <= SPACE) && whitespaceChars[c])
			   {
				 if(breakTerm)  // break the term with spacial charater 
				 {
					 breakTerm = false; 
					 todo = false;
				   	 break;
				  }
				  switch (val)
				  {
				  case TT_UNKNOWN:
					 val	= TT_WHITESPACE;
					 break;
				  case TT_WHITESPACE:
					 break;
				  default:		   // could end a tag or a regular word
					 reset	= true;
					 todo	= false;
				  }
			   }
			   else			   // process non-whitespace characters
			   {
				   if(breakTerm)  // break the term with spacial charater 
				   {
					   breakTerm = false; 
					   todo = false;
					   reset = true;  // use the previous character
					   break;
				   }
				  switch (val)
				  {
				  case TT_UNKNOWN:
					 switch (c)
					 {
					 case '<':
						val	= TT_TAG;
						break;
					 case '&':
						val	= TT_ENTITY;
						break;	
					 default:
						buffer.append(c);
						val	= TT_WORD;
					 }
					 break;
				  case TT_TAG:
					 if (buffer.length() == 0)
					 {
						if (!(Character.isLetter(c) || (c == '/') || (c == '!')))
						{		   
							// not a real tag
							buffer.append('<');
							buffer.append(c);
							val	= TT_WORD;
						}
						else
						{
						   buffer.append(c);
						}
					 }
					 else if ((buffer.length() == 2) && (buffer.charAt(0) == '!') &&
							  (buffer.charAt(1) == '-') && (c == '-'))
					 {
						return processComment();
					 }
					 else
					 {
						if (c == '>')
						{
						   todo	= false;
						   reset= true;
						}
						else
						{
						   buffer.append(c);
						}
					 }
					 break;
				  case TT_WHITESPACE:	   // pushBack non whitespace & emit w no sval
					 reset	= true;
					 todo	= false;
					 break;
				  case TT_ENTITY:
					 switch (c)
					 {
					 case '&':	   // funny ways to end an entity
					 case '<':
					 case '>':
						reset	= true;
						// drop through!
					 case ';':
						todo	= false;
						break;
					 default:
						// let Parser lookup find its just a word starting w &
						if (count >= 7)
						   todo	= false;
						buffer.append(c);
					 }
					 break;
					 // already processing some type of word
				  default:	// TT_WORD
					 switch (c)
					 {
					 case '<':
						//		     if (prevWasLessThan)
						//		     {
						//			buffer.append(c);
						//			break;
						//		     }
					 case '&':
						todo	= false;
						reset	= true;
						break;
						
					// Parsing the terms such as abc/edf to 'abc/' 'edf'
					// This should display without "space after" on
					// by eunyee 
					 case '/': // parsing for the url terms such as http://www.abc.com or abc/def
					 case '-': // parsing for the terms like abc-edf
					 case ':':
					 case ',':
					 case '.':
					 case '(':
					 case ')':
					 case '[':
					 case ']':
					 case '{':
					 case '}':
					 case '|':
					 case '\\':
					 case '\'':
					 case '+':
					 case '%':
					 case '^':
					 case '#':
					 case '*':
					 case '$':
					 case '?':
					 case '!':
					 case '~':
					 case '_':
					 case '=':
					 case '@': //email
					 	buffer.append(c);
					breakTerm = true;
					 	break;
					 	
					 default:
						buffer.append(c);
					 }
				  }
			   }
			   break;
			case INSIDE_TAG_MODE:
			   // whitespace signifies end of an attr
			   // whitespace cannot be returned as val
			   // looking for end tag, unquoted_attr_val, start of val
			   // looking for words - attrs
			   // space and equals   =  transitions
			   switch (c)
			   {
			   case ' ':
			   case '\n':
			   case '\r':
			   case '\t':
				  if (buffer.length() > 0)
				  {
					 todo		= false;
					 val		= TT_ATTR;
				  }
				  // else ignore leading whitespace and continue!
				  break;
			   case '>':
				  if (buffer.length() > 0) // an attr is waiting to be emitted
				  {
					 reset	= true;	// pushback & return to else just below
					 val	= TT_ATTR;
				  }
				  else
				  {
					 val	= TT_TAG_CLOSE;
					 mode	= OUTSIDE_TAG_MODE;
				  }
				  todo		= false;
				  break;
			   case '=':
				  if (buffer.length() > 0) // an attr is waiting to be emitted
				  {
					 reset	= true;	// pushback & return to else just below
					 val	= TT_ATTR;
					 todo	= false;
				  }
				  else
					 mode	= VAL_MODE;
				  break;
			   default:
				  if (c > 0x20)
				  {
					  // toss control characters
					 buffer.append(c);
				  }
			   }	       
			   break;
			case VAL_MODE:
			   // we've seen an equals sign. looking for words
			   // start quote, space   =  transitions
			   switch (c)
			   {
			   case ' ':
			   case '\n':
			   case '\r':
			   case '\t':
				  if (buffer.length() > 0)
				  {
					 val	= TT_UNQUOTED_VAL;
					 mode	= INSIDE_TAG_MODE;
					 todo	= false;
				  }
				  // else ignore leading whitespace and continue!
				  break;
			   case '>':
				  if (buffer.length() > 0) // an attr is waiting to be emitted
				  {
					 val	= TT_UNQUOTED_VAL;
					 mode	= INSIDE_TAG_MODE;
					 reset	= true;	// pushback & return to else above
				  }
				  else
				  {
					 val	= TT_TAG_CLOSE;
					 mode	= OUTSIDE_TAG_MODE;
				  }
				  todo	= false;
				  break;
			   case '\'':
			   case '"':
				  if (buffer.length() == 0)
				  {
					 quoteChar	= c;
					 mode		= QUOTED_VAL_MODE;
					 break;
				  }
				  // else fall through (a rather bogus attr name was coded)
			   default:
				  buffer.append(c);
			   }
			   break;
			case QUOTED_VAL_MODE:
			   // end quote   = only transition
			   // whitespace is not significant
			   // end tag ignored
			   switch (c)
			   {
			   case ' ':
			   case '\n': // still delete leading whitespace
			   case '\r':
			   case '\t':
				  if (buffer.length() > 0)
					 buffer.append(" "); // turn into plain space
				  break;
			   default:
				  if (c == quoteChar)
				  {
					 todo	= false;
					 mode	= INSIDE_TAG_MODE;
					 val	= TT_QUOTED_VAL;
				  }
				  else
					 buffer.append(c);
				  break;
			   }	       
			case COMMENT_MODE:
			   // whitespace is not significant
			   // end comment = only transition
			   //	    System.out.println("COMMENT " + c +" w sval=" + sval);
			   if (sval.endsWith("--"))
			   {
				  dashes = 2;
			   }
			   switch (c)
			   {
			   case '-':
				  dashes++;
				  break;
			   case '>':
				  if (dashes >= 2)
				  {
					 val	= TT_END_COMMENT;
					 todo	= false;
					 mode	= OUTSIDE_TAG_MODE;
				  }
				  break;
			   default:
				  dashes	= 0;
			   }
			   break;
			   case SCRIPT_COMMENT_MODE:
					switch(c)
					{
						case '\n':
							mode = OUTSIDE_TAG_MODE;
							todo = false;
							break;
						default:
							break;	
					}
				
				break;
			default:
			   //	    Env.error("Scan.scan() programmer error. Unknown mode="+mode);
			   debug("scan() programmer error. Unknown mode="+mode);
			   todo	= false;
			}
			
		 }
		 if (reset)
		 {
			prevChar	= c;
			usePrev	= true;
		 }
		 // find whitespace
		 // find tag start
		 // if inside tag, find tag end or = or double quote
		 switch (val)
		 {
		 case TT_TAG:
			mode	= INSIDE_TAG_MODE;
			// fall-through
		 case TT_ENTITY:
		 case TT_ATTR:
			StringTools.toLowerCase(buffer);
			sval	= StringTools.toString(buffer);
			break;
		 case TT_WORD:
		 case TT_UNQUOTED_VAL:
		 case TT_QUOTED_VAL:
			sval	= StringTools.toString(buffer);
			break;
		 case TT_END_COMMENT:
			//	 System.out.println("END_COMMENT");
			// case TT_WHITESPACE:
		 default:
			sval	= null;
		 }
		 return val;
	  }
      /*
	  public static void main(String args[])
	  {
      String arg	= args[0];
      InputStream stream	= null;
      if (arg.startsWith("http://"))
      {
	  URL url	= HTMLPage.newURL(null, arg, "", true, true);
	  try
	  {
	  stream
	  = url.openStream();
	  } catch (Exception e) { System.out.println(e); }
	  
      }
      else
	  try
	  {
	  stream		= new FileInputStream(Files.newFile(arg));
	  } catch (Exception e) { System.out.println(e); }
      Scan scanner		= new Scan(new BufferedInputStream(stream));
      int	val;
      try
      {
	  do 
	  {
	  val	= scanner.scan();
	  System.out.println("token = " + val + "\t" + scanner.sval
	  + "\tmode=" + scanner.mode);
	  if ((val == TT_TAG) && scanner.sval.startsWith("!--"))
	  {
	  System.out.println("\nCOMMENT_MODE");
	  scanner.mode	= COMMENT_MODE;
	  }
	  } while (val != TT_EOF);
      } catch (Exception e)
	  {
	  System.out.println(e);
	  e.printStackTrace();
	  }
	  
	  }
	  */
   private int processComment()
	  throws IOException
	  {
		 boolean dash1 = false;
		 boolean dash2 = false;
		 //println("start COMMENT, ignoring: ");
		 do
		 {
			int ic	= bufferedReader.read();;
			
			if (ic == -1)
			{
			   return TT_EOF;
			}
			
			char c		= (char) ic;
			//System.err.print(c);
			if (dash2)
			{
			   if (c == '>')
			   {
				   //println("\nreturn TT_COMMENT");
				  return TT_COMMENT;
			   }
			   else
			   {
				  dash1= false;
				  dash2= false;
			   }
			}
			else if (dash1)
			{
			   if (c == '-')
			   {
				  dash2= true;
			   }
			   else
			   {
				  dash1= false;
			   }
			}
			else
			{
			   if (c == '-')
			   {
				  dash1= true;
			   }
			}

		 } while (true);
	  }
   
   public BufferedReader bufferedReader()
   {
  	 return bufferedReader;
   }
}
