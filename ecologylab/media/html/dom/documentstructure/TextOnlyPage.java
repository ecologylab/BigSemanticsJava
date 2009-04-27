package ecologylab.media.html.dom.documentstructure;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.tidy.TdNode;

import ecologylab.media.html.dom.HTMLElement;
import ecologylab.media.html.dom.ParagraphText;
import ecologylab.media.html.dom.RecognizedDocumentStructure;
import ecologylab.media.html.dom.TidyInterface;
import ecologylab.media.html.dom.utils.StringBuilderUtils;
import ecologylab.xml.XMLTools;


/**
 * This is for the text-only page. 
 * This page might contain images but those images are not informative images, thus we recognize those pages with non-informative images 
 * as text-only pages. 
 * 
 * @author eunyee
 * @author andruid
 */
public class TextOnlyPage extends RecognizedDocumentStructure
{
	private static final int	MIN_PARA_TEXT_LENGTH	= 25;
	public static final int			MAX_TEXT_SURROGATES	= 5;
	
	/**
	 * Generate only text surrogates 
	 */
	protected void generateSurrogates(TdNode articleMain, ArrayList<HTMLElement> imgNodes, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTexts, TidyInterface htmlType)
	{
		int count	= 0;
		while (!paraTexts.isEmpty() && count<MAX_TEXT_SURROGATES)
		{
			ParagraphText paraText		= paraTexts.remove(paraTexts.lastKey());
			generateTextSurrogate(paraText, htmlType);
		}
	}

	private void generateTextSurrogate(ParagraphText paraText, TidyInterface htmlType)
	{
		StringBuilder paraTextBuffy	= paraText.getPtext();
		
		if (paraTextBuffy.length()>MIN_PARA_TEXT_LENGTH)
		{
			XMLTools.unescapeXML(paraTextBuffy);
			htmlType.newTxt(paraTextBuffy);//, paraTexts.node );
		}
		
		paraText.recycle();	// releases to pool
	}
}