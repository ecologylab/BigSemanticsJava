package ecologylab.semantics.html.standalone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;

import org.w3c.tidy.DOMDocumentImpl;
import org.w3c.tidy.Node;
import org.w3c.tidy.Out;
import org.w3c.tidy.OutJavaImpl;
import org.w3c.tidy.StreamIn;

import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.DOMWalkInformationTagger;
import ecologylab.semantics.html.ImgElement;
import ecologylab.semantics.html.documentstructure.RecognizedDocumentStructure;


@Deprecated
public class ContentBodyRecognize extends OldHTMLDOMParser
{
	RecognizedDocumentStructure recPageType;
	
	@Override
	public org.w3c.dom.Document parse(PURLConnection purlConnection)
	{
		recPageType	= new RecognizedDocumentStructure(purlConnection.getPurl());
		return super.parse(purlConnection);
	}

	public Node pprint(org.w3c.dom.Document doc, OutputStream out, ParsedURL purl)
	{
		Out o = new OutJavaImpl(this.getConfiguration(), null);
		Node document;

		if (!(doc instanceof DOMDocumentImpl)) {
			return null;
		}
		document = ((DOMDocumentImpl)doc).adaptee;

//		o.state = StreamIn.FSM_ASCII;
//		o.encoding = configuration.CharEncoding;

		//      if (out != null)
			//      {
			// Instantiate PPrint constructor that connects to combinFormation
		DOMWalkInformationTagger pprint = new DOMWalkInformationTagger(configuration, purl, null);

//		o.out = out;
		if (configuration.xmlTags)
			pprint.printXMLTree(o, (short)0, 0, null, document);
		else
			pprint.printTree(o, (short)0, 0, null, document);

		pprint.flushLine(o, 0);

		Node articleMain = RecognizedDocumentStructure.recognizeContentBody(pprint);

		if( articleMain!=null )
		{
//			recPageType.findImgsInContentBodySubTree(articleMain.parent(), imgNodes);
			informativeImages();
		}

		return articleMain;

	}

	protected void informativeImages()
	{
		for(int i=0; i<recPageType.getImgNodesInContentBody().size(); i++ )
		{
			ImgElement imgElement = recPageType.getImgNodesInContentBody().get(i);

			ParsedURL imgPurl 			= imgElement.getSrc();
			int width 					= imgElement.getWidth();
			int height 					= imgElement.getHeight();

			float aspectRatio 	= (float)width / (float)height;
			aspectRatio 				= (aspectRatio>1.0) ?  (float)1.0/aspectRatio : aspectRatio;

			String altStr 			= imgElement.getAlt();
			boolean parentHref 	= imgElement.getNode().parent().element.equals("a");  		
			boolean articleImg 	= true;

			// Advertisement Keyword in the "alt" value
			if( altStr!=null && altStr.toLowerCase().contains("advertis") )  
				articleImg = false;

			//FIXME -- andruid -- restore this!!!
			/*
			if( imgUrl!=null )
			{
				//FIXME -- use compiled regex!
				String urlChunks[] = imgUrl.split("/");
				for (int j=0; j<urlChunks.length; j++)
				{
					String temp = urlChunks[j].toLowerCase();
					//	System.out.println("url Chunk:" + temp);
					if (temp.equals("adv") || temp.contains("advertis") ) // || temp.equals("ad")
					{
						articleImg = false;
						break;
					}
				}
			}
	*/

			if( (width!=-1 && width<100) || (height!=-1 && height<100) )
				articleImg = false;

			if( articleImg )
			{
				recPageType.getImgNodesInContentBody().add(imgElement);
			}

		}
	}


	protected String getContentBody(ParsedURL labelFilePurl, String contentBodyID)
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
	
	static final ParsedURL TEST_COLLECITON_BASE	= ParsedURL.getAbsolute("http://csdll.cs.tamu.edu:9080/TestCollections/websites/ResearchArticle/");;

	///*
	public static void main(String args[])
	{
		ContentBodyRecognize cbr = new ContentBodyRecognize();
		try 
		{
			File ff = new File( "researchSites.txt"); //"folderList.txt" )  

			InputStream ii = new FileInputStream(ff);
			BufferedReader myInput 	= new BufferedReader(new InputStreamReader(ii));

			String temp = null;
			while( (temp=myInput.readLine())!=null )
			{
				ParsedURL	purl			= TEST_COLLECITON_BASE.getRelative(temp.trim() + "/");
				ParsedURL labelPurl	= purl.getRelative("label.xml");
				System.out.println(purl.toString());
				PURLConnection purlConnection	= purl.connect();
				Node contentBodyNode = cbr.pprint( cbr.parseDOM(purlConnection.inputStream(), null), null, purl); 
				PURLConnection labelConnection	= labelPurl.connect();
				try
				{
					if ((labelConnection != null) && (labelConnection.urlConnection().getContent()!=null) 
							&& (contentBodyNode!=null) && (contentBodyNode.getAttrByName("tag_id")!=null) )
					{
						String returnVal=cbr.getContentBody(labelPurl, contentBodyNode.getAttrByName("tag_id").value);
						if( returnVal.equals("no") )
							System.out.println("WHY NOT THIS!!!!!!!!!!! " + purl);
					}
					else 
						System.out.println("NO LABE for this document");

				}
				catch(FileNotFoundException e)
				{
					continue;
				}
				finally
				{
					purlConnection.recycle();
					if (labelConnection != null)
						labelConnection.recycle();
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