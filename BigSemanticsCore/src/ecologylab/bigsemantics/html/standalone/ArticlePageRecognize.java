package ecologylab.bigsemantics.html.standalone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Node;

import ecologylab.bigsemantics.html.DOMWalkInformationTagger;
import ecologylab.bigsemantics.html.documentstructure.RecognizedDocumentStructure;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;


/**
 * 
 * @author eunyee
 */
@Deprecated
public class ArticlePageRecognize extends OldHTMLDOMParser
{
    private static final String	PORTAL_PURL	= "http://portal.acm.org/browse_dl.cfm?coll=ACM&dl=ACM&idx=J961&linked=1&part=transaction";

		/**
     * Pretty-prints a DOM Document.
     * Extract Image and Text Surrogates while walk through DOM
     */
    public void pprint(org.w3c.dom.Document doc, OutputStream out, ParsedURL purl)
    {
//        Out o = new OutJavaImpl(this.getConfiguration(), null);
    	StringWriter o = new StringWriter();
        Node document;

//        if (!(doc instanceof DOMDocumentImpl)) {
//            return;
//        }
        document = doc.getDocumentElement();

//        o.state = StreamIn.FSM_ASCII;
//        o.encoding = configuration.CharEncoding;

  //      if (out != null)
  //      {
        	// Instantiate PPrint constructor that connects to combinFormation
        DOMWalkInformationTagger pprint = new DOMWalkInformationTagger(purl, null);
        
//        o.out = out;
//        if (configuration.xmlTags)
//            pprint.printXMLTree(o, (short)0, 0, null, document);
//        else
            pprint.tagTree(document);
        
        Node articleMain = RecognizedDocumentStructure.recognizeContentBody(pprint);
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
				
			ParsedURL purl 								= ParsedURL.getAbsolute(PORTAL_PURL);
			PURLConnection purlConnection	= purl.connect();
			InputStream in 								= purlConnection.inputStream();
			apr.pprint( apr.parseDOM(in, null), null, purl);
			
			in.close();
			
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