package ecologylab.media.html.dom.documentstructure;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.tidy.TdNode;

import ecologylab.media.html.dom.ImgNodewithAttr;
import ecologylab.media.html.dom.ParagraphText;
import ecologylab.media.html.dom.RecognizedDocumentStructure;
import ecologylab.media.html.dom.TidyInterface;
import ecologylab.xml.XMLTools;


/**
 * This is for the text-only page. 
 * This page might contain images but those images are not informative images, thus we recognize those pages with non-informative images 
 * as text-only pages. 
 * 
 * @author eunyee
 *
 */
public class TextOnlyPage extends RecognizedDocumentStructure
{
	/**
	 * Generate only text surrogates 
	 */
	protected void generateSurrogates(TdNode articleMain, ArrayList<ImgNodewithAttr> imgNodes, int totalTxtLeng, 
			TreeMap<Integer, ParagraphText> paraTexts, TidyInterface htmlType)
	{
		int size = paraTexts.size();
		if( size>0 )
		{
	//		ParagraphText paraText		= (ParagraphText) paraTexts.values().toArray()[size-1];

			ParagraphText paraText		= paraTexts.get(paraTexts.lastKey());
			StringBuilder paraTextStr	= paraText.getPtext();
			XMLTools.unescapeXML(paraTextStr);
			
			if( paraTextStr.length()>25 )
				htmlType.newTxt( paraTextStr.toString() );//, paraTexts.node );
		}
	}
}