/**
 * 
 */
package ecologylab.semantics.html.utils;

import org.w3c.dom.Node;
import org.w3c.dom.Text;

import ecologylab.generic.StringBuilderBaseUtils;
import ecologylab.generic.StringTools;

/**
 * 
 *
 * @author andruid 
 */
public class StringBuilderUtils extends StringBuilderBaseUtils
{
	/**
	 * Trim whitespace off of the buffer referred to through pointers in the TdNode.
	 * If result is not empty, then decode the resulting text into a StringBuilder.
	 * <p/>
	 * For the result, an existing StringBuilder will be cleared (reset), or,
	 * if result == null on entry, a new StringBuilder is acquired from the pool.
	 * <p/>
	 * If there is a non-null result, it is up to the caller to call release(result) when it is no longer needed.
	 * 
	 * @param result			The StringBuilder we're using. Either the one passed in (and perhaps modified), if there was one,
	 * 										or a new one, if null was passed in and there are actually chars to decode after trim.
	 * @param childNode		TdNode of source. Supplies byte array of characters, and start and end points.
	 * 
	 * @return						null if no work to do because length < minLength after trim, or a buffer with the decoded result.
	 */
  public static StringBuilder trimAndDecodeUTF8(StringBuilder result, Node childNode)
  {
  	return trimAndDecodeUTF8(result, childNode, 0, false);
  }

	/**
	 * Trim whitespace off of the buffer referred to through pointers in the TdNode.
	 * If result is longer than minLength, then decode the resulting text into a StringBuilder.
	 * <p/>
	 * For the result, an existing StringBuilder will be cleared (reset), or,
	 * if result == null on entry, a new StringBuilder is acquired from the pool.
	 * <p/>
	 * If there is a non-null result, it is up to the caller to call release(result) when it is no longer needed.
	 * 
	 * @param result			The StringBuilder we're using. Either the one passed in (and perhaps modified), if there was one,
	 * 										or a new one, if null was passed in and there are actually chars to decode after trim.
	 * @param childNode		TdNode of source. Supplies byte array of characters, and start and end points.
	 * @param minLength		A threshold that is applied to decide if resulting chars should be decoded to result.
	 * 										For unconditional decode, set to 0.
	 * 
	 * @return						null if no work to do because length < minLength after trim, or a buffer with the decoded result.
	 */
  public static StringBuilder trimAndDecodeUTF8(StringBuilder result, Node childNode, int minLength)
  {
  	return trimAndDecodeUTF8(result, childNode, minLength, false);
  }
	
	/**
	 * Trim whitespace off of the buffer referred to through pointers in the TdNode.
	 * If result is longer than minLength, then decode the resulting text into a StringBuilder.
	 * <p/>
	 * If appendNoClear is true, then append the new text to the old StringBuilder passed in as result (concatenate).
	 * Otherwise, an existing StringBuilder will be cleared (reset).
	 * <p/>
	 * If result == null on entry, a new StringBuilder is acquired from the pool.
	 * It is up to the caller to call release(result) when it is no longer needed.
	 * 
	 * @param result			The StringBuilder we're using. Either the one passed in (and perhaps modified), if there was one,
	 * 										or a new one, if null was passed in and there are actually chars to decode after trim.
	 * @param childNode		TdNode of source. Supplies byte array of characters, and start and end points.
	 * @param minLength		A threshold that is applied to decide if resulting chars should be decoded to result.
	 * 										For unconditional decode, set to 0.
	 * @param appendNoClear If true, we append to prior results without clearing them.
	 * 
	 * @return						null if no work to do because length < minLength after trim, or a buffer with the decoded result.
	 */
  
  //FIXME - textarray(), start(), end()
  public static StringBuilder trimAndDecodeUTF8(StringBuilder result, Node childNode, int minLength, boolean appendNoClear)
  {
		byte[] textarray	= null;
		
		if (childNode instanceof Text)
		{
			textarray = childNode.getNodeValue().getBytes();
		}
		else
			textarray = childNode.getNodeName().getBytes();
		
//		int start				 	= childNode.start();
//		int end 					= childNode.end();
//		
//		// trim in place				
//		while (Character.isWhitespace((char) textarray[start]) && (start < end))
//		{
//			start++;
//		}
//		while (Character.isWhitespace((char) textarray[end - 1]) && (start < end))
//		{
//			end--;
//		}
		int length	= textarray.length;
		if (length > minLength)
		{
			if (!((length >= 4) && (textarray[0] == '<') &&
					 (textarray[1] == '!') && (textarray[2] == '-') && (textarray[3] == '-')))
			{
				if (result == null) 
					result			= acquire();
				else if (!appendNoClear)
					StringTools.clear(result);

				StringTools.decodeUTF8(result, textarray, 0, length);
			}
		}
		return result;
  }
}
