package ecologylab.media.html.dom;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
	protected static final int PARA_TEXT_LENGTH_LIMIT = 80;
	TidyInterface htmlType;
	int encoding;
	int state;

	/**
	 * Keep the array of the paragraph texts in the article body.
	 * 
	 */
	protected TreeMap<Integer, ParagraphText>	paragraphTexts	= new TreeMap<Integer, ParagraphText>();

	/**
	 * Current DOM node that is being processed
	 */
	protected TdNode	currentNode		= null;

	/**
	 * Collection of text elements until a block level element is reached
	 */
	protected ParagraphText pt = new ParagraphText();

	/**
	 * Keep track of the text length in this page to recognize the page type. 
	 */
	protected int 		totalTxtLength  = 0; 

	/**
	 * All images in the page
	 */
	private ArrayList<HtmlNodewithAttr> allImgNodes			= new ArrayList<HtmlNodewithAttr>();

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
		String p = node.element;

		// Check out the attributesMap later and Fix this  -- EUNYEE
		HashMap attributesMap = new HashMap(20);
		addAttributes(node.attributes, attributesMap);

		if( htmlType != null )
		{
			if( "title".equals(p) ) 
			{
				htmlType.setTitle(node);
			}
			else if( "a".equals(p) ) 
			{
				HtmlNodewithAttr attrNode = new HtmlNodewithAttr(node, attributesMap);
				allAnchorNodes.add(attrNode);
				//This call is performed during the second parse while generating containers and extracting metadata.
				//htmlType.newAHref(attributesMap);
			}
			else if( "i".equals(p) ) 
			{
				htmlType.setItalic(true);
			}
			else if( "b".equals(p) )
			{
				htmlType.setBold(true);
			}	
			else if( "img".equals(p) )
			{   
				HtmlNodewithAttr ina = new HtmlNodewithAttr(node, attributesMap);
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

		pt = new ParagraphText();
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
				StringBuilder pstr = getStringBuilder(textarray, start, end-start);

				StringTools.trim(pstr);
				if(!((pstr.length() == 4) && (pstr.charAt(0) == 'n') &&
						(pstr.charAt(1) == 'u') && (pstr.charAt(2) == 'l') && (pstr.charAt(3) == 'l')))
				{
					// Update the total text length for this page. 
					totalTxtLength = totalTxtLength + pt.append(pstr);

					pt.setNode(currentNode);

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
		if( paragraphTexts.size() > 10 )
		{
			Integer tkey = paragraphTexts.firstKey(); 
			if( tkey.intValue() < totalTxtLength )
			{
				paragraphTexts.remove(tkey);
				paragraphTexts.put(totalTxtLength, pt);
			}

		}
		// We don't put the text into the paragraphTexts structure unless the text is over certain length and not surrounded by <a> tag. 
		else if( (totalTxtLength > PARA_TEXT_LENGTH_LIMIT) && !underAHref(currentNode) )
			paragraphTexts.put(totalTxtLength, pt);
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

	/**
	 * See what is the better way to integrate HashMap to existing attribute structure of Tidy.
	 */
	public static void addAttributes(AttVal attr, HashMap attrMap)
	{
		if (attr != null)
		{
			attrMap.put(attr.attribute, attr.value);
			if (attr.next != null)
				addAttributes(attr.next, attrMap);
		}
	}

	
	public ArrayList<HtmlNodewithAttr> getAllAnchorNodes()
	{
		return allAnchorNodes;
	}

}