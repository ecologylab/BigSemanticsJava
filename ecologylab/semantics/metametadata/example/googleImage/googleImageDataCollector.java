package ecologylab.semantics.metametadata.example.googleImage;

import org.junit.Test;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.metametadata.example.MyInfoCollector;
import ecologylab.semantics.metametadata.example.generated.GeneratedMetadataTranslationScope;

public class googleImageDataCollector
{
	//List<GoogleImage> metadataCollected = ArrayList<GoogleImage>(); 
	
	public static void main(String[] args) throws InterruptedException
	{
		//SemanticAction.register(googleImageSemanticAction.class);
		
		MyInfoCollector mic = new MyInfoCollector("repo", GeneratedMetadataTranslationScope.get());
		
		ParsedURL pUrl = ParsedURL.getAbsolute("http://www.google.com/images?q=texas+site%3Awww.nytimes.com");
		
		mic.getContainerDownloadIfNeeded(null, pUrl, null, false, false, false);
		
		Thread.sleep(10000); 
		
		mic.getDownloadMonitor().stop(); 
		
		//print out result to file 
		
		
		
		//print out result to database 
		
		
	}
	
	@Test
	public void testPUrl(){
		ParsedURL pUrl = ParsedURL.getAbsolute("http://www.google.com/images?q=texas+site%3Awww.nytimes.com");
		System.out.println(pUrl);
		
		
	}
	
}
 