package ecologylab.semantics.html;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.documentstructure.ImageFeatures;
import ecologylab.semantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.semantics.html.utils.HTMLAttributeNames;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.serialization.XMLTools;

/**
 * Walking through DOM and tag necessary information
 * 
 * Extends Jtidy's PPrint object to keep state necessary for image+text surrogate extraction
 * 
 * @author eunyee
 * 
 */
public class DOMWalkInformationTagger implements HTMLAttributeNames
{
	protected static final int							MAX_LINKS_PER_PAGE			= 200;

	protected static final int							PARA_TEXT_LENGTH_LIMIT	= 80;

	DOMParserInterface											parserInterface;

	ParsedURL																purl;

	int																			encoding;

	int																			state;

	/**
	 * Current DOM node that is being processed
	 */
	protected org.w3c.dom.Node							currentNode							= null;

	/**
	 * Keep track of the text length in this page to recognize the page type.
	 */
	protected int														totalTxtLength					= 0;

	/**
	 * Collection of text elements until a block level element is reached
	 */
	protected ParagraphText									currentParagraphText		= new ParagraphText();

	/**
	 * Keep the array of the paragraph texts in the article body.
	 * 
	 */
	private TreeMap<Integer, ParagraphText>	paragraphTextsTMap			= new TreeMap<Integer, ParagraphText>();

	/**
	 * All images in the page
	 */
	protected ArrayList<ImgElement>					allImgNodes							= new ArrayList<ImgElement>();

	/**
	 * All links in current page
	 */
	protected ArrayList<AElement>						allAnchorNodes					= new ArrayList<AElement>();

	private int[]														linebuf;

	private int															lbufsize;

	private int															linelen;

	String																	partitionID							= "";

	FileOutputStream												fileOutputStream				= null;
	

	public DOMWalkInformationTagger(ParsedURL purl, DOMParserInterface parserInterface)
	{
		this.purl 						= purl;
		this.parserInterface 	= parserInterface;
	}
	
	public void generateCollections(Node doc)
	{
		Element root = null;
		NodeList list = doc.getChildNodes();
		for (int i=0; i<list.getLength(); i++)
		{
			if (list.item(i) instanceof Element)
			{
				root = (Element)list.item(i);
				break;
			}
		}

		generateCollectionsFromRoot(root);
	}

	public void generateCollectionsFromRoot(Node root)
	{
  	tagTree(root);
	}

	public void tagTree(Node node)
	{
		Node content;
		currentNode = node;
		if (node == null)
			return;
		
		short nodeType = node.getNodeType();
		switch (nodeType)
		{
		case Node.TEXT_NODE:
			if (node.getNodeValue() != null && node.getNodeValue().length() > 0)
				tagText(node.getNodeValue().getBytes(), 0, node.getNodeValue().length(), node);
			break;
		case Node.ELEMENT_NODE:
			if (!node.getNodeName().toLowerCase().equals("script") && (!node.getNodeName().toLowerCase().equals("style")))
			{
				printTag(node);
				NodeList children = node.getChildNodes();
				for (int i=0; i<children.getLength(); i++)
				{
					Node child = children.item(i);
					tagTree(child);
				}
				printEndTag(node);
			}
			break;
		case Node.DOCUMENT_NODE:
			NodeList children = node.getChildNodes();
			for (int i=0; i<children.getLength(); i++)
			{
				content = children.item(i);
				tagTree(content);
			}
			break;
		default:
			printTag(node);
			NodeList children2 = node.getChildNodes();
			for (int i=0; i<children2.getLength(); i++)
			{
				Node child = children2.item(i);
				tagTree(child);
			}
			printEndTag(node);
      	
		}
	}
	
	public void printTag(Node node)
	{
		String tagName = node.getNodeName().toLowerCase();

		if (tagName.equals("img"))
		{
			ImgElement imgElement = new ImgElement(node, purl);
			// TODO confirm that we are happy only collecting images that seem informative
			if (imgElement.isInformativeImage())
				allImgNodes.add(imgElement);
		}
		else if (tagName.equals("base"))
		{
			Node baseHrefAttr = node.getAttributes().getNamedItem("href");
			String baseHref = (baseHrefAttr == null) ? null : baseHrefAttr.getNodeValue();
			if (baseHref != null)
				purl = (purl == null) ? ParsedURL.getAbsolute(baseHref) : purl.getRelative(baseHref);
		}
		else if (parserInterface != null)
		{
			if (tagName.equals("title"))
			{
				parserInterface.setTitle(node);
			}
			else if (tagName.equals("a"))
			{
				if (allAnchorNodes.size() < MAX_LINKS_PER_PAGE)
				{
					AElement attrNode = new AElement(node, purl);
					allAnchorNodes.add(attrNode);
				}
				// This call is performed during the second parse while generating containers and extracting metadata
				// htmlType.newAHref(attributesMap);
			}
		}
		else if (tagName.equals("i"))
		{
			parserInterface.setItalic(true);
		}
		else if (tagName.equals("b"))
		{
			parserInterface.setBold(true);
		}
		
		//We need to delete a link to the file write part at the end -- EUNYEE

	}

	protected void printEndTag(Node node)
	{
		String tag = node.getNodeName().toLowerCase();

		if (parserInterface != null)
		{
			if (tag.equals("i"))
				parserInterface.setItalic(false);
			else if (tag.equals("b"))
				parserInterface.setBold(false);
			// Create a new Paragraph text based on these tags
			// TODO add more tags that we should define as starting of a new paragraph -- eunyee
			if (tag.equals("p") || tag.equals("br") || tag.equals("td") || tag.equals("div")
					|| tag.equals("li") || tag.equals("a") || tag.equals("option")
					|| (tag.length() == 2 && tag.startsWith("h")))
			{
				closeBlock(node);
			}
		}	
	}

	private void closeBlock(Node blockNode)
	{
		addCompletedPara(blockNode);
		currentParagraphText = new ParagraphText();
		totalTxtLength = 0;
	}

	protected void tagText(byte[] textarray, int start, int end, Node node)
	{
		if (textarray != null && textarray.length > 0)
		{
			if (!(currentNode.getParentNode().getAttributes().getNamedItem("style") != null))
			{
				while (Character.isWhitespace((char) textarray[start]) && (start < end - 1))
				{
					start++;
				}
				while (Character.isWhitespace((char) textarray[end - 1]) && (start < end - 1))
				{
					end--;
				}

				int length = end - start;

				if (length > 0
						&& !(length == 4 && textarray[0] == 'n' && textarray[1] == 'u' && textarray[2] == 'l' && textarray[3] == 'l'))
				{
					currentParagraphText.append(textarray, start, end);
					totalTxtLength += length;
					currentParagraphText.setNode(node);
				}
			}
		}
	}

	/**
	 * Associate an actual paragraph text with the current node if one wasn't already. If appropriate,
	 * add the currentParagraphText to the paragraphTextsMap. (For example, if there aren't too many
	 * already or if this one looks longer than those collected.) Otherwise, recycle the
	 * currentParagraphText.
	 * 
	 * @param blockNode
	 */
	protected void addCompletedPara(Node blockNode)
	{
		Node node = currentNode;
		if (!currentParagraphText.hasText())
		{
			StringBuilder longestTxtInSubTree = RecognizedDocumentStructure.getLongestTxtinSubTree(
					blockNode, null);
			if (longestTxtInSubTree != null)
			{
				if (longestTxtInSubTree.length() > PARA_TEXT_LENGTH_LIMIT)
				{
					currentParagraphText.setNode(blockNode);
					currentParagraphText.setBuffy(longestTxtInSubTree);
					node = blockNode;
				}
				else
					StringBuilderUtils.release(longestTxtInSubTree);
			}
		}

		if (currentParagraphText.hasText())
		{
			int length = currentParagraphText.length();
			/*
			 * Only keeps 10 paragraph texts. Thus, if there is a new paragraph text coming in and the 10
			 * slots have been already filled, we replace with the existing one based on the length of the
			 * text.
			 */
			if (paragraphTextsTMap.size() > 10)
			{
				Integer tkey = paragraphTextsTMap.firstKey();
				if (tkey.intValue() < totalTxtLength)
				{
					ParagraphText removed = paragraphTextsTMap.remove(tkey);
					removed.recycle();
					paragraphTextsTMap.put(totalTxtLength, currentParagraphText);
				}
				else
					currentParagraphText.recycle();
			}
			
			// We don't put the text into the paragraphTexts structure unless the text is over certain
			// length and not surrounded by <a>
			else if ((length > PARA_TEXT_LENGTH_LIMIT) && !underAHref(node) && node.getNodeType() != Node.COMMENT_NODE && (!(node.getNodeName().toLowerCase().equals("script")) || (node.getNodeName().toLowerCase().equals("style"))))
			{
				// FIXME -- look out for duplicates introduced by getLongestTxtinSubTree() above
				paragraphTextsTMap.put(length, currentParagraphText);
			}
			else
				currentParagraphText.recycle();
		}
		else
			currentParagraphText.recycle();
	}

	public boolean underAHref(Node node)
	{
		if ((node.getParentNode().getParentNode().getNodeName().equals("a"))
				|| (node.getParentNode().getNodeName().equals("a")))
		{
			return true;
		}
		return false;
	}

	int startID(String idValue)
	{
		String startID = idValue.substring(0, idValue.indexOf('_'));
		int sID = Integer.parseInt(startID);
		return sID;
	}

	int endID(String idValue)
	{
		String endID = idValue.substring(idValue.indexOf('_') + 1);
		int eID = Integer.parseInt(endID);
		return eID;
	}

	void checkInPartitionID(Node node, int wordSize, int aWordSize)
	{
		NamedNodeMap attributes = node.getParentNode().getAttributes();
		String nodeID = attributes.getNamedItem("tag_id").getNodeValue();
		String data = "";

		if ((startID(nodeID) >= startID(partitionID)) && (endID(nodeID) <= endID(partitionID)))
			data = nodeID + ", " + wordSize + ", " + aWordSize + ", " + "inform" + "\n";
		else
			data = nodeID + ", " + wordSize + ", " + aWordSize + ", " + "non_inform" + "\n";

		try
		{
			fileOutputStream.write(data.getBytes());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public synchronized void recycle()
	{
		if (paragraphTextsTMap != null)
		{
			for (ParagraphText pt : paragraphTextsTMap.values())
			{
				pt.recycle();
			}
			paragraphTextsTMap.clear();
			paragraphTextsTMap = null;
		}
		recycle(allImgNodes);
		allImgNodes = null;
		recycle(allAnchorNodes);
		allAnchorNodes = null;

		currentNode = null;
	}

	private static void recycle(Collection<? extends HTMLElementDOM> nodeCollection)
	{
		if (nodeCollection != null)
		{
			for (HTMLElementDOM thatNode : nodeCollection)
				thatNode.recycle();
		}
	}

	public static StringBuilder getTextInSubTree(Node node, boolean recurse)
	{
		return getTextInSubTree(node, recurse, null, false, false);
	}

	/**
	 * Non-recursive method to get the text for the <code>node</code> Collects the text even if the
	 * node contains other nodes in between, specifically the <code>anchor</code>. It does not however
	 * include the text from the anchor node.
	 * 
	 * @param node
	 * @param appendNewline TODO
	 * @param te
	 * @return
	 */
	// FIXME -- why is text` in anchor node not included?

	public static StringBuilder getTextInSubTree(Node node, boolean recurse, StringBuilder result, boolean appendNewline, boolean ignoreAltText)
	{
		NodeList children = node.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node childNode = children.item(i);
			if ((recurse && childNode.hasChildNodes())
					&& (!childNode.getNodeName().toLowerCase().equals("script")) && (!childNode.getNodeName().toLowerCase().equals("style")))
			{
				result = getTextInSubTree(childNode, true, result, appendNewline, ignoreAltText);
			}
			else if (childNode.getNodeType() == Node.TEXT_NODE)
			{
				int length = 0;

				if (result != null)
				{
					result.append(' ');
					length = result.length();
				}
				result = StringBuilderUtils.trimAndDecodeUTF8(result, childNode, 0, true);

				if (result != null)
				{
					if (length == result.length())
						result.setLength(length - 1);
					else if (appendNewline)
						result.append('\n');
				}
			}
		  //images now alt text to the caption of the image
			else if (!ignoreAltText && childNode.getNodeName().toLowerCase().equals("img"))
			{
				NamedNodeMap attributes = childNode.getAttributes();
				Node altAtt = attributes.getNamedItem(ALT);
				String alt = (altAtt != null) ? altAtt.getNodeValue() : null;

				if (!ImageFeatures.altIsBogus(alt))
				{
					if (result == null)
						result = StringBuilderUtils.acquire();
					else
						result.append(' ');
					result.append(alt);
				}
			}
		}

		if (result != null)
			XMLTools.unescapeXML(result);

		return result;
	}

	public static StringBuilder getStringBuilder(byte[] bytes, int offset, int length)
	{
		try
		{
			return new StringBuilder(new String(bytes, offset, length, "UTF8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new Error("UTF-8 to string conversion failed: " + e.getMessage());
		}
	}

	public int getTotalTxtLength()
	{
		return totalTxtLength;
	}

	public ArrayList<ImgElement> getAllImgNodes()
	{
		return allImgNodes;
	}

	public ArrayList<AElement> getAllAnchorNodes()
	{
		return allAnchorNodes;
	}

	public TreeMap<Integer, ParagraphText> getParagraphTextsTMap()
	{
		return paragraphTextsTMap;
	}

	public void setPartitionID(String partitionID)
	{
		this.partitionID = partitionID;
	}

	public void setFileOutputStream(FileOutputStream fos)
	{
		this.fileOutputStream = fos;
	}
}
