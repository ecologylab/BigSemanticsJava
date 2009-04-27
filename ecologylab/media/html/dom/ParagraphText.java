package ecologylab.media.html.dom;

import org.w3c.tidy.TdNode;

import ecologylab.generic.StringBuilderPool;
import ecologylab.media.html.dom.utils.StringBuilderUtils;

/**
 * Keep the paragraph text in the document with the DOM Node to recognize the ArticleMain node.
 * 
 * @author eunyee
 *
 */    
public class ParagraphText
{
	private StringBuilder	buffy;
	private TdNode 			node;
	
	public ParagraphText()
	{
//		ptext = new String();
	}
	
	public TdNode getNode()
	{
		return node;
	}

	public void setNode(TdNode node)
	{
		this.node = node;
	}

	public StringBuilder getPtext() 
	{
		return buffy;
	}
	
	/**
	 * Append the argument to the buffy inside.
	 * If buffy was not empty at the start of this operation, append a space first.
	 * 
	 * @param toAppend
	 * @return	The number of characters added.
	 */
	public int append(CharSequence toAppend) 
	{
		int	result	= toAppend.length();
		if (buffy == null)
			//TODO -- should this be built larger? how many calls are made on average?
			buffy			= StringBuilderUtils.acquire();
		
		buffy.append(' ');
		buffy.append(toAppend);
		result++;

		return result;
	}
	
	public void append(byte[] bytes, int start, int end)
	{
		if (buffy == null)
			//TODO -- should this be built larger? how many calls are made on average?
			buffy			= StringBuilderUtils.acquire();

		while (start < end)
		{
			buffy.append((char) bytes[start++]);
		}
	}
	
	public void recycle()
	{
		if (buffy != null)
			StringBuilderUtils.release(buffy);
		buffy				= null;
	}
}

