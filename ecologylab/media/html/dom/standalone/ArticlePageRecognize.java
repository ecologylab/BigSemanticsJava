package ecologylab.media.html.dom.standalone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.tidy.DOMDocumentImpl;
import org.w3c.tidy.Out;
import org.w3c.tidy.OutImpl;
import org.w3c.tidy.StreamIn;
import org.w3c.tidy.TdNode;

import ecologylab.media.html.dom.DOMWalkInformationTagger;
import ecologylab.media.html.dom.HTMLDOMParser;
import ecologylab.media.html.dom.RecognizedDocumentStructure;


/**
 * 
 * @author eunyee
 */
public class ArticlePageRecognize extends HTMLDOMParser
{
    /**
     * Pretty-prints a DOM Document.
     * Extract Image and Text Surrogates while walk through DOM
     */
    public void pprint(org.w3c.dom.Document doc, OutputStream out, String url)
    {
        Out o = new OutImpl();
        TdNode document;

        if (!(doc instanceof DOMDocumentImpl)) {
            return;
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
//System.out.println("ArticleMain: " + articleMain );

        if( articleMain == null )
        {
        	nonArticlePage++;
        	System.out.println("NON ARTICLE PAGE!!!!!!!!!!!!! ");
        }
        else
        {
        	articlePage++;
        	System.out.println("YES!!!!!!!! ARTICLE PAGE!!!!!!!!!!!");
        }

    }
    
    int articlePage = 0;
    int nonArticlePage = 0;
    
//   /*
	public static void main(String args[])
	{
		ArticlePageRecognize apr = new ArticlePageRecognize();
		URL url;
		try 
		{
			/*
			  File ff = new File( "NonArticle-folderList.txt" ); // "researchIndex.txt");
//			File ff = new File("folderList.txt");
			
			  InputStream ii = new FileInputStream(ff);
			  BufferedReader myInput 	= new BufferedReader(new InputStreamReader(ii));

			  String temp = null;
			  while( (temp=myInput.readLine())!=null )
			  {
				String urlString = "http://csdll.cs.tamu.edu:9080/TestCollections/websites/ResearchIndex/" + temp + "/";
				url = new URL(urlString);
				System.out.println(urlString);
				
				String labelURLStr = urlString + "label.xml";

System.out.println("\n\n" + urlString );				
//				URL labelURL = new URL(labelURLStr);
//				if( (labelURL!=null) && (labelURL.openConnection()!=null) && (labelURL.getContent()!=null)  )
//				{
								
					InputStream in = url.openConnection().getInputStream();
					apr.pprint( apr.parseDOM(in, null), null, urlString); 
					System.out.println("DONE \n");
//				}
			  }
			  
			  System.out.println("ArticlePage: " + apr.articlePage + "  NonArticlePage: " + apr.nonArticlePage );
			  double sum = apr.articlePage + apr.nonArticlePage; 
			  System.out.println("ArticlePage: " + (double)apr.articlePage/sum);
			  System.out.println("NonArticlePage: " + (double)apr.nonArticlePage/sum);
				*/
				
			url = new URL("http://portal.acm.org/browse_dl.cfm?coll=ACM&dl=ACM&idx=J961&linked=1&part=transaction");
			InputStream in = url.openConnection().getInputStream();
			apr.pprint( apr.parseDOM(in, null), null, url.toString()); 
			
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
		ArticlePageRecognize apr = new ArticlePageRecognize();
		String urlString = "http://csdll.cs.tamu.edu:9080/TestCollections/websites/News/1178399895044/";
		URL url;
		try {
			url = new URL(urlString);
			InputStream in = url.openConnection().getInputStream();
			apr.pprint(apr.parseDOM(in, null), null, urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	*/
}