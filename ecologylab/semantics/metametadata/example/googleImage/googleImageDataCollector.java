package ecologylab.semantics.metametadata.example.googleImage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.junit.Test;

import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticAction;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metametadata.example.MyInfoCollector;

public class googleImageDataCollector
{

	 //ref. Document <- Media <- Image <- GoogleImage  
	//static List<GoogleImage> metadataCollected = new ArrayList<GoogleImage>(); 
		
	public static void main(String[] args) throws InterruptedException
	{ 
	
	}

	@SuppressWarnings("unchecked")
	@Test
	/**
	 * Main test method 
	 */
	public void testCollector() throws InterruptedException, FileNotFoundException{
		SemanticAction.register(googleImageSemanticAction.class);
		
		MyInfoCollector mic = new MyInfoCollector("repo", GeneratedMetadataTranslationScope.get());
		
		ParsedURL pUrl = ParsedURL.getAbsolute("http://www.google.com/images?q=texas+site%3Awww.nytimes.com");
		
		//ParsedURL pUrl2 = ParsedURL.getAbsolute("http://www.google.com/images?q=texas");
		
		mic.getContainerDownloadIfNeeded(null, pUrl, null, false, false, false);
		
		Thread.sleep(10000); 
		
		mic.getDownloadMonitor().stop(); 
		
		//	print out result to file 
//		printOutToFile("googleImage.out", metadataCollected);
		
		//print out result to database
		
	}
	
	
	/**
	 * print out to file 
	 * @param filename
	 * @param metadataCollected
	 * @throws FileNotFoundException
	 */
	private void printOutToFile(String filename, List metadataCollected) throws FileNotFoundException{
		PrintWriter pw = new PrintWriter(new FileOutputStream(filename));
		StringBuffer sb = new StringBuffer("#format:title,size,srcUrl,refUrl\n"); 
		
		if(metadataCollected.size() > 0){
			for (Object obj : metadataCollected)
			{
//				if(obj instanceof GoogleImage){
					//sb.append(((GoogleImage) obj).getTitle() + " " + obj.getSize() + " " + obj.getSrcUrl() + " " + obj.getRefUrl() + "\n");
					
//				}
			}
			
		}else
			Debug.println("no results retrieved");
		
		pw.write(sb.toString()); 
		pw.flush(); 
		pw.close(); 
		
	}
	
	/**
	 * connect db -> stmt -> executeUpdate
	 * db schema generated from GoogleImage.class automatically using <link>SqlTranslator</link>
	 * ref. DBUtil.class   
	 *  
	 * @param metadataCollected 
	 */
	private void printoutToDB(List metadataCollected){
		
		
	}
	
	@Deprecated
	@Test
	public void testPUrl(){
		ParsedURL pUrl = ParsedURL.getAbsolute("http://www.google.com/images?q=texas+site%3Awww.nytimes.com");
		System.out.println(pUrl.getRelative("ecologylab"));
		
	}
	
}
 