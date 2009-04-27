/**
 * 
 */
package ecologylab.media.html.dom.utils;

import org.w3c.tidy.TdNode;

import ecologylab.generic.StringBuilderPool;
import ecologylab.generic.StringTools;

/**
 * 
 *
 * @author andruid 
 */
public class StringBuilderUtils
{

	/**
	 * string builder pool to parse link metadata
	 */
	private static final StringBuilderPool stringBuilderPool = new StringBuilderPool(30, 512);
	
	public static StringBuilder acquire()
	{
		return stringBuilderPool.acquire();
	}
	public static void release(StringBuilder buffy)
	{
		stringBuilderPool.release(buffy);
	}
	
  public static StringBuilder decodeTrimmedUTF8(StringBuilder result, TdNode childNode, int minLength)
  {
		byte[] textarray	= childNode.textarray();
		
		int start				 	= childNode.start();
		int end 					= childNode.end();
		
		// trim in place				
		while (Character.isWhitespace((char) textarray[start]) && (start < end))
		{
			start++;
		}
		while (Character.isWhitespace((char) textarray[end - 1]) && (start < end))
		{
			end--;
		}
		int length	= end-start;
		if (length > minLength)
		{
			if (!((length >= 4) && (textarray[0] == '<') &&
					 (textarray[1] == '!') && (textarray[2] == '-') && (textarray[3] == '-')))
			{
				if (result == null) 
					result			= acquire();
				else
					StringTools.clear(result);

				StringTools.decodeUTF8(result, textarray, start, length);
			}
		}
		return result;
  }
}
