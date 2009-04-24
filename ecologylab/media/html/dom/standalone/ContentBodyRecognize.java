package ecologylab.media.html.dom.standalone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.w3c.tidy.DOMDocumentImpl;
import org.w3c.tidy.Out;
import org.w3c.tidy.OutImpl;
import org.w3c.tidy.StreamIn;
import org.w3c.tidy.TdNode;

import ecologylab.generic.Generic;
import ecologylab.media.html.dom.DOMWalkInformationTagger;
import ecologylab.media.html.dom.HTMLDOMParser;
import ecologylab.media.html.dom.HtmlNodewithAttr;
import ecologylab.media.html.dom.RecognizedDocumentStructure;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;



public class ContentBodyRecognize extends HTMLDOMParser
{
	RecognizedDocumentStructure recPagetype = new RecognizedDocumentStructure();
	
    public TdNode pprint(org.w3c.dom.Document doc, OutputStream out, String url)
    {
        Out o = new OutImpl();
        TdNode document;

        if (!(doc instanceof DOMDocumentImpl)) {
            return null;
        }
        document = ((DOMDocumentImpl)doc).adaptee;

        o.state = StreamIn.FSM_ASCII;
        o.encoding = configuration.CharEncoding;

  //      if (out != null)
  //      {
        	// Instantiate PPrint constructor that connects to combinFormation
        DOMWalkInformationTagger pprint = new DOMWalkInformationTagger(configuration, null);
        pprint.setState( StreamIn.FSM_ASCII );
        pprint.setEncoding( configuration.CharEncoding );
        
        o.out = out;
        if (configuration.XmlTags)
            pprint.printXMLTree(o, (short)0, 0, null, document);
        else
            pprint.printTree(o, (short)0, 0, null, document);

        pprint.flushLine(o, 0);
        
        TdNode articleMain = RecognizedDocumentStructure.recognizeContentBody(pprint);
        
        if( articleMain!=null )
        {
        	recPagetype.findImgsInContentBodySubTree(articleMain.parent());
	        informativeImages();
        }
        
        return articleMain;

    }
    
    protected void informativeImages()
    {
    	for(int i=0; i<recPagetype.getImgNodesInContentBody().size(); i++ )
    	{
    		HtmlNodewithAttr ina = (HtmlNodewithAttr) recPagetype.getImgNodesInContentBody().get(i);
    		
    		String imgUrl = ina.getAttribute("src");
    		int width 		= ina.getAttributeAsInt("width");
    		int height 		= ina.getAttributeAsInt("height");
    		
    		float aspectRatio = (float)width / (float)height;
    		aspectRatio 			= (aspectRatio>1.0) ?  (float)1.0/aspectRatio : aspectRatio;
    		
    		String altStr 			= ina.getAttribute("alt");
    		boolean parentHref 	= ina.getNode().parent().element.equals("a");  		
    		boolean articleImg 	= true;

    		// Advertisement Keyword in the "alt" value
    		if( altStr!=null && altStr.toLowerCase().contains("advertis") )  
    			articleImg = false;
    		
    		if( imgUrl!=null )
    		{
  			
	    		String urlChunks[] = imgUrl.split("/");
	    		for(int j=0; j<urlChunks.length; j++)
	    		{
	    			String temp = urlChunks[j];
	    		//	System.out.println("url Chunk:" + temp);
	    			if( /*temp.toLowerCase().equals("ad") ||*/ temp.toLowerCase().equals("adv") ||
	    					temp.toLowerCase().contains("advertis") )
	    				articleImg = false;
	    		}
    		}
    		
    		if( (width!=-1 && width<100) || (height!=-1 && height<100) )
    			articleImg = false;
    		
    		if( articleImg )
    		{
    			recPagetype.getImgNodesInContentBody().add(ina);
    		}
    		
    	}
    }

    
	protected String getContentBody(URL labelFile, String contentBodyID)
	{
/*		try 
		{
			DocumentState ds = (DocumentState) ElementState.translateFromXML(labelFile, TranslationScope.get("collectionBrowseServlet", "collectionBrowseServlet"));
			if( ds!=null )
			{
				totalLabeledDocument++;
				
				PartitionState partitionState = ds.getPartitionSet().get(0);
			
				String mainPartitionTag_ID = partitionState.getTag_id();
System.out.println("contentBody Tag_ID : " + contentBodyID + "   mainPartitionTag_ID:" + mainPartitionTag_ID);
				if( contentBodyID.equals(mainPartitionTag_ID) )
				{
					System.out.println("ContentBody equals to main partition Tag_ID");
					correctContentBody++;
					return "yes";
				}
				
				
				InformTextSet informTextSet = partitionState.getInformTextSet();
				for( int i=0; i<informTextSet.size(); i++ )
				{
					InformTextState informText = informTextSet.get(i);
					String informTextID = informText.getTag_id();
System.out.println("informTextID : " + informTextID);					
					if( contentBodyID.equals(informTextID) )
					{
						System.out.println("Main partition Tag_ID equals to informTextID ");
						correctContentBody++;
						return "yes";
					}
				}
*/				
				/*
				InformImgSet informImgSet = partitionState.getInformImgSet();
				for( int i=0; i<informImgSet.size(); i++ )
				{
					InformImgState informImgState = informImgSet.get(i);
					String informImgTag_ID = informImgState.getTag_id();
					ArrayList imageNodes = this.getArticleImgNodes();
					for(int j=0; j<imageNodes.size(); j++ )
					{
						TdNode node = (TdNode) imageNodes.get(j);
						String imgNodeID = node.getAttrByName("tag_id").value;
//System.out.println(" ImageNodeID=" + imgNodeID + "   informImgTag_ID=" + informImgTag_ID);						
						if( imgNodeID.equals(informImgTag_ID) )
						{
							correctImage++;
							return "yes";
						}
					}
				}
				*/
				
/*			}
		} 
		catch (XMLTranslationException e) 
		{
			e.printStackTrace();
		}
		*/
		return "no";
	}
	
	int totalLabeledDocument = 0;
	int correctContentBody = 0;
	int correctImage = 0;
	
///*
	public static void main(String args[])
	{
		ContentBodyRecognize cbr = new ContentBodyRecognize();
		URL url;
		try 
		{
			  File ff = new File( "researchSites.txt"); //"folderList.txt" )  
			
			  InputStream ii = new FileInputStream(ff);
			  BufferedReader myInput 	= new BufferedReader(new InputStreamReader(ii));

			  String temp = null;
			  while( (temp=myInput.readLine())!=null )
			  {
				
				String urlString = "http://csdll.cs.tamu.edu:9080/TestCollections/websites/ResearchArticle/" + temp.trim() + "/";
				String labelURLStr = urlString + "label.xml";
				
				url = new URL(urlString);
				System.out.println(urlString);
				InputStream in = url.openConnection().getInputStream();
				TdNode contentBodyNode = cbr.pprint( cbr.parseDOM(in, null), null, urlString); 
System.out.println("\n\n" + urlString );				
				URL labelURL = new URL(labelURLStr);
				try
				{
					if( (labelURL!=null) && (labelURL.openConnection()!=null) && (labelURL.getContent()!=null) 
							&& (contentBodyNode!=null) && (contentBodyNode.getAttrByName("tag_id")!=null) )
					{
						String returnVal=cbr.getContentBody(labelURL, contentBodyNode.getAttrByName("tag_id").value);
						if( returnVal.equals("no") )
							System.out.println("WHY NOT THIS!!!!!!!!!!! " + urlString);
					}
					else 
						System.out.println("NO LABE for this document");

				}
				catch(FileNotFoundException e)
				{
					continue;
				}
								
				System.out.println("\n");
			  }

			  System.out.println("STAT : " + cbr.totalLabeledDocument + " : " + cbr.correctContentBody
					  + " :  images = " + cbr.correctImage );

			
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}		
	}
	
//	*/
	/*
	public static void main(String args[])
	{
		ContentBodyRecognize cbr = new ContentBodyRecognize();
		String urlString = "http://csdll.cs.tamu.edu:9080/TestCollections/websites/Article/1204410974562/";
		String labelURLStr = urlString + "label.xml";
		
		try{
			URL url = new URL(urlString);
			System.out.println(urlString);
			InputStream in = url.openConnection().getInputStream();
			TdNode contentBodyNode = cbr.pprint( cbr.parseDOM(in, null), null, urlString); 
	System.out.println("\n\n" + urlString );				
			URL labelURL = new URL(labelURLStr);
	
			if( (labelURL!=null) && (labelURL.openConnection()!=null) && (labelURL.getContent()!=null) 
					&& (contentBodyNode!=null) && (contentBodyNode.getAttrByName("tag_id")!=null) )
			{
				String returnVal=cbr.getContentBody(labelURL, contentBodyNode.getAttrByName("tag_id").value);
				if( returnVal.equals("no") )
					System.out.println("WHY NOT THIS!!!!!!!!!!! " + urlString);
			}
			else 
				System.out.println("NO LABE for this document");

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("\n");
		
	}
	*/
}