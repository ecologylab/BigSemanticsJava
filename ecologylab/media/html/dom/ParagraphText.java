package ecologylab.media.html.dom;

import org.w3c.tidy.TdNode;

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
			buffy			= new StringBuilder(toAppend);
		else
		{
			buffy.append(' ');
			buffy.append(toAppend);
			result++;
		}
		return result;
	}
}

