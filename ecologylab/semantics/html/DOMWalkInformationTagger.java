package ecologylab.semantics.html;

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
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.utils.StringBuilderUtils;



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
	protected static final int 	PARA_TEXT_LENGTH_LIMIT 	= 80;
	
	TidyInterface htmlType;
	
	ParsedURL			purl;
	
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
	private ArrayList<ImgElement> 	allImgNodes			= new ArrayList<ImgElement>();

	/**
	 * All links in current page
	 */
  protected ArrayList<AElement> allAnchorNodes		= new ArrayList<AElement>();

	public DOMWalkInformationTagger(Configuration configuration, ParsedURL purl, TidyInterface htmlType) 
	{
		super(configuration);
		this.purl			= purl;
		this.htmlType = htmlType;
	}

	/**
	 * This method is called when it sees the Starting-Tag while walking through DOM
	 */
	@Override
	protected void printTag(Lexer lexer, Out fout, short mode, int indent, TdNode node)
	{
		String tagName = node.element;

		if( "img".equals(tagName) )
		{   
			ImgElement imgElement = new ImgElement(node, purl);
			//TODO confirm that we are happy only collecting images that seem informative
			if (imgElement.isInformativeImage())
				allImgNodes.add(imgElement);
		}
		else if ("base".equals(tagName))
		{
			AttVal baseHrefAttr = node.getAttrByName("href");
			String baseHref			= (baseHrefAttr == null) ? null : baseHrefAttr.value;
			if (baseHref != null)
				purl			= (purl == null) ? ParsedURL.getAbsolute(baseHref) : purl.getRelative(baseHref);
		}
		else if( htmlType != null )
		{
			if( "title".equals(tagName) ) 
			{
				htmlType.setTitle(node);
			}
			else if( "a".equals(tagName) ) 
			{
				if(allAnchorNodes.size() < MAX_LINKS_PER_PAGE)
				{
					AElement attrNode = new AElement(node, purl);
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
		String tag = node.element;

		if( htmlType != null )
		{
			if( tag.equals("a") )
			{
				htmlType.closeHref();
			}
			else if( tag.equals("i") )
			{
				htmlType.setItalic(false);
			}
			else if( tag.equals("b") )
			{
				htmlType.setBold(false);
			}
//			if ("h1".equals(tag) || "p".equals(tag))
//			{
//				System.out.println(RecognizedDocumentStructure.getLongestTxtinSubTree(node, null));
//			}
			// Create a new Paragraph text based on these tags
			// TODO add more tags that we should define as starting of a new paragraph. -- eunyee
			if ( tag.equals("p") || tag.equals("br") || tag.equals("td") || tag.equals("div") || tag.equals("li") || tag.equals("a")
					|| tag.equals("tr") || tag.equals("option") 
					|| (tag.length() == 2 && tag.startsWith("h")))
			{
				closeBlock(node);
			}
		}

		//		 We need to delete a link to the file write part at the end -- EUNYEE
		super.printEndTag(fout, mode, indent, node);	
	}

	private void closeBlock(TdNode blockNode)
	{
		addCompletedPara(blockNode);

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

	public ArrayList<ImgElement> getAllImgNodes()
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

	/**
	 * Associate an actual paragraph text with the current node if one wasn't already.
	 * If appropriate, add the currentParagraphText to the paragraphTextsMap.
	 * (For example, if there aren't too many already or if this one looks longer than those collected.)
	 * Otherwise, recycle the currentParagraphText.
	 * 
	 * @param blockNode
	 */
	private void addCompletedPara(TdNode blockNode)
	{
		TdNode node	= currentNode;
		if (currentParagraphText.length() == 0)
		{
			StringBuilder longestTxtinSubTree = RecognizedDocumentStructure.getLongestTxtinSubTree(blockNode, null);
			if (longestTxtinSubTree != null)
			{
				if (longestTxtinSubTree.length() > PARA_TEXT_LENGTH_LIMIT)
				{
					currentParagraphText.setNode(blockNode);
					currentParagraphText.setBuffy(longestTxtinSubTree);
					node			= blockNode;
				}
				else
					StringBuilderUtils.release(longestTxtinSubTree);
			}
		}
		int length	= currentParagraphText.length();
		/*
		 * Only keeps 10 paragraph texts. 
		 * Thus, if there is a new paragraph text coming in and the 10 slots have been already filled, we replace with the existed one based on the length of the text.
		 */
		if( paragraphTextsTMap.size() > 10 )
		{
			Integer tkey = paragraphTextsTMap.firstKey(); 
			if( tkey.intValue() < totalTxtLength )
			{
				ParagraphText removed = paragraphTextsTMap.remove(tkey);
				removed.recycle();
				paragraphTextsTMap.put(totalTxtLength, currentParagraphText);
			}
			else
				currentParagraphText.recycle();
		}
		// We don't put the text into the paragraphTexts structure unless the text is over certain length and not surrounded by <a> tag. 
		else if( (length > PARA_TEXT_LENGTH_LIMIT) && !underAHref(node) )
		{
			//FIXME -- look out for duplicates introduced by getLongestTxtinSubTree() above
			paragraphTextsTMap.put(length, currentParagraphText);
		}
		else
			currentParagraphText.recycle();
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
	
	public ArrayList<AElement> getAllAnchorNodes()
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

	private static void recycle(Collection<? extends HTMLElement> nodeCollection)
	{
		for (HTMLElement thatNode: nodeCollection)
			thatNode.recycle();
	}
}