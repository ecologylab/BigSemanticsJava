package ecologylab.media.html.dom;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import org.w3c.tidy.AttVal;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Lexer;
import org.w3c.tidy.Out;
import org.w3c.tidy.PPrint;
import org.w3c.tidy.TdNode;

import ecologylab.generic.StringTools;



/**
 * Walking through DOM and tag necessary information 
 * 
 * Extends Jtidy's PPrint object to keep state necessary for image+text surrogate extraction
 * 
 * @author eunyee
 *
 */
public class DOMWalkInformationTagger extends PPrint
{
	private static final int		MAX_LINKS_PER_PAGE			= 200;
	protected static final int PARA_TEXT_LENGTH_LIMIT 	= 80;
	
	TidyInterface htmlType;
	
	int 					encoding;
	int 					state;

	/**
	 * Keep the array of the paragraph texts in the article body.
	 * 
	 */
	private TreeMap<Integer, ParagraphText>	paragraphTextsTMap	= new TreeMap<Integer, ParagraphText>();

	protected TreeMap<Integer, ParagraphText> getParagraphTextsTMap()
	{
		return paragraphTextsTMap;
	}

	/**
	 * Current DOM node that is being processed
	 */
	protected TdNode				currentNode		= null;

	/**
	 * Collection of text elements until a block level element is reached
	 */
	protected ParagraphText currentParagraphText = new ParagraphText();

	/**
	 * Keep track of the text length in this page to recognize the page type. 
	 */
	protected int 					totalTxtLength  = 0; 

	/**
	 * All images in the page
	 */
	private ArrayList<HtmlNodewithAttr> 	allImgNodes			= new ArrayList<HtmlNodewithAttr>();

	/**
	 * All links in current page
	 */
  protected ArrayList<HtmlNodewithAttr> allAnchorNodes = new ArrayList<HtmlNodewithAttr>();

	public DOMWalkInformationTagger(Configuration configuration, TidyInterface htmlType) 
	{
		super(configuration);
		this.htmlType = htmlType;
	}

	/**
	 * This method is called when it sees the Starting-Tag while walking through DOM
	 */
	@Override
	protected void printTag(Lexer lexer, Out fout, short mode, int indent, TdNode node)
	{
		String tagName = node.element;

		if( htmlType != null )
		{
			if( "title".equals(tagName) ) 
			{
				htmlType.setTitle(node);
			}
			else if( "a".equals(tagName) ) 
			{
				if(allAnchorNodes.size() < MAX_LINKS_PER_PAGE)
				{
					HtmlNodewithAttr attrNode = new HtmlNodewithAttr(node);
					allAnchorNodes.add(attrNode);	
				}
				//This call is performed during the second parse while generating containers and extracting metadata.
				//htmlType.newAHref(attributesMap);
			}
			else if( "i".equals(tagName) ) 
			{
				htmlType.setItalic(true);
			}
			else if( "b".equals(tagName) )
			{
				htmlType.setBold(true);
			}	
			else if( "img".equals(tagName) )
			{   
				HtmlNodewithAttr ina = new HtmlNodewithAttr(node);
				allImgNodes.add(ina);
			}

		}

		// We need to delete a link to the file write part at the end -- EUNYEE
		super.printTag(lexer, fout, mode, indent, node);
	}

	/**
	 * This method is called when it sees the Ending-Tag while walking through DOM
	 */
	@Override
	protected void printEndTag(Out fout, short mode, int indent, TdNode node)
	{
		String p = node.element;

		if( htmlType != null )
		{
			if( p.equals("a") )
			{
				htmlType.closeHref();
			}
			else if( p.equals("i") )
			{
				htmlType.setItalic(false);
			}
			else if( p.equals("b") )
			{
				htmlType.setBold(false);
			}
			// Create a new Paragraph text based on these tags
			// TODO add more tags that we should define as starting of a new paragraph. -- eunyee
			if ( p.equals("p") || p.equals("br") || p.equals("td") || p.equals("div") || p.equals("li") || p.equals("a")
					|| p.equals("tr") || p.equals("option") 
					|| (p.length() == 2 && p.startsWith("h")))
			{
				closeBlock();
			}
		}

		//		 We need to delete a link to the file write part at the end -- EUNYEE
		super.printEndTag(fout, mode, indent, node);	
	}

	private void closeBlock()
	{
		addCompletedPara();

		currentParagraphText = new ParagraphText();
		totalTxtLength = 0;
	}

	@Override
	protected void outterSupport(TdNode node, short mode)
	{
		super.outterSupport(node, mode);
		this.currentNode = node;
	}


	public int getTotalTxtLength()
	{
		return totalTxtLength;
	}

	public ArrayList<HtmlNodewithAttr> getAllImgNodes()
	{
		return this.allImgNodes;
	}

	//FIXME use CharBuffers throughout!!!
	public static StringBuilder getStringBuilder(byte[] bytes, int offset, int length) {
		try {
			return new StringBuilder(new String(bytes, offset, length, "UTF8"));
		} catch (java.io.UnsupportedEncodingException e) {
			throw new Error("UTF-8 to string conversion failed: " + e.getMessage());
		}
	}

	/**
	 * Look carefully about character encoding so that cF can support different encoding languages
	 */
	@Override
	protected void printText(Out fout, short mode, int indent,
			byte[] textarray, int start, int end)
	{
		if( (mode == 0) && (currentNode!=null) && (textarray != null))
		{
			if( currentNode.parent().element.equals("div") && (currentNode.parent().getAttrByName("style")!=null))
			{
				// This fixes the problem of newly introduced "<div style=""> </div>" format which defines style with div TAG!!! - eunyee
			}
			else
			{
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
				if((length > 0) && !((length == 4) && (textarray[0] == 'n') &&
						(textarray[1] == 'u') && (textarray[2] == 'l') && (textarray[3] == 'l')))
				{
					currentParagraphText.append(textarray, start, end);
					// Update the total text length for this page. 
					totalTxtLength += length;

					currentParagraphText.setNode(currentNode);

					flushLine(fout, indent);	
				}
			}
		}
	}

	private void addCompletedPara()
	{
		/*
		 * Only keeps 10 paragraph texts. 
		 * Thus, if there is a new paragraph text coming in and the 10 slots have been already filled, we replace with the existed one based on the length of the text.
		 */
		if( paragraphTextsTMap.size() > 10 )
		{
			Integer tkey = paragraphTextsTMap.firstKey(); 
			if( tkey.intValue() < totalTxtLength )
			{
				paragraphTextsTMap.remove(tkey);
				paragraphTextsTMap.put(totalTxtLength, currentParagraphText);
			}

		}
		// We don't put the text into the paragraphTexts structure unless the text is over certain length and not surrounded by <a> tag. 
		else if( (totalTxtLength > PARA_TEXT_LENGTH_LIMIT) && !underAHref(currentNode) )
			paragraphTextsTMap.put(totalTxtLength, currentParagraphText);
	}

	public boolean underAHref(TdNode node)
	{
		if(node.grandParent().element.equals("a") || node.parent().element.equals("a"))
			return true;

		return false;
	}

	String partitionID = "";
	public void setPartitionID(String id)
	{
		this.partitionID = id;
	}

	FileOutputStream fileoutputstream = null;
	public void setFileOutputStream(FileOutputStream fos)
	{
		this.fileoutputstream = fos;
	}

	int startID(String idValue)
	{
		String startID = idValue.substring(0, idValue.indexOf('_'));
		int sID = Integer.parseInt(startID);
		return sID;
	}

	public void setState(int s)
	{
		this.state = s;
	}

	public void setEncoding(int enc)
	{
		this.encoding = enc;
	}

	int endID(String idValue)
	{
		String endID = idValue.substring(idValue.indexOf('_')+1);
		int eID = Integer.parseInt(endID);
		return eID;
	}

	void checkInPartitionID(TdNode node, int wordSize, int aWordSize)
	{
		String nodeID = node.parent().getAttrByName("tag_id").value;
		String data = "";
		if( (startID(nodeID)>=startID(partitionID)) && (endID(nodeID)<=endID(partitionID)) )
		{
			data = nodeID + ", " + wordSize + ", " + aWordSize + ", " + "inform" + "\n";
		}
		else
		{
			data = nodeID + ", " + wordSize + ", " + aWordSize + ", " + "non_inform" + "\n";
		}

		try 
		{
			fileoutputstream.write(data.getBytes());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public ArrayList<HtmlNodewithAttr> getAllAnchorNodes()
	{
		return allAnchorNodes;
	}
	
	public void recycle()
	{
		for (ParagraphText pt: paragraphTextsTMap.values())
		{
				pt.recycle();
		}
		paragraphTextsTMap.clear();
		
		recycle(allImgNodes);
		allImgNodes			= null;
		recycle(allAnchorNodes);
		allAnchorNodes	= null;
		
		currentNode			= null;
	}

	private static void recycle(Collection<HtmlNodewithAttr> nodeCollection)
	{
		for (HtmlNodewithAttr thatNode: nodeCollection)
			thatNode.recycle();
	}
}