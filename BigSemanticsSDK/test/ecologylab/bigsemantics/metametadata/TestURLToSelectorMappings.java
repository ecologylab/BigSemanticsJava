package ecologylab.bigsemantics.metametadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ecologylab.bigsemantics.generated.library.RepositoryMetadataTranslationScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.tools.MmTest;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;



@RunWith(Parameterized.class)
public class TestURLToSelectorMappings {

	//setup constructs
	class TestMmd extends MmTest
	{

		public MetaMetadataRepository getMetaMetadataRepository()
		{
			return this.semanticsSessionScope.getMetaMetadataRepository();
		}

		public TestMmd(String appName) throws SIMPLTranslationException {
			super(appName);
		}
		
	}
	MetaMetadataRepository getRepository()
	{
		return null;
	}

	private String testUrl = "";
	private String expectedMmd;
	private String foundMmd;
	static int i = 0;
	public TestURLToSelectorMappings(Object[] tripple)
	{
		this.testUrl = (String)tripple[0];
		this.expectedMmd = (String)tripple[1];
		this.foundMmd =  (String)tripple[2];
	}
	
	
	//Found structrue from http://stackoverflow.com/questions/358802/junit-test-with-dynamic-number-of-tests
    @Parameters
    public static Collection<Object[]> getFiles() {
    	//Setup that constucts a parameterized test for each example URL
    	Collection<Object[]> params = new ArrayList<Object[]>();
    	TestURLToSelectorMappings accessClass = new TestURLToSelectorMappings(new Object[]{"","",""});
    		TestMmd testMmd = null;
    		try {
    			testMmd = accessClass.new TestMmd("TestMmdExampleCoverage");
    		} catch (SIMPLTranslationException e1) {
    			fail("Can't load MetaMetadata repository!");
    		}
    		MetaMetadataRepository repository = testMmd.getMetaMetadataRepository();
    		
    		/***
    		 * 
    		 * 
    		 * Remove me do not push
    		 */
    		
    		File repoScopeFile = new File("/tmp/mmd_repo_scope.json");
    		FileOutputStream repoScopeOutstream = null;
			try {
				repoScopeOutstream = new FileOutputStream(repoScopeFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		try {
				SimplTypesScope.serialize(RepositoryMetadataTranslationScope.get(),repoScopeOutstream, Format.JSON);
			} catch (SIMPLTranslationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		File repoInstanceFile = new File("/tmp/mmd_repo_instance.json");
    		FileOutputStream repoInstanceStream = null;
			try {
				repoInstanceStream = new FileOutputStream(repoInstanceFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		try {
				SimplTypesScope.serialize(repository,repoInstanceStream, Format.JSON);
			} catch (SIMPLTranslationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		
    		ArrayList<String> mmdsByName = repository.getMMNameList();
    		for(String mmdName : mmdsByName)
    		{
    			///System.out.println("MMD:"+mmdName);
    			MetaMetadata mmd = repository.getMMByName(mmdName);
    			ArrayList<ExampleUrl> examples = mmd.getExampleUrls();
    			if(examples != null)
    			{
    				////System.out.println("No example urls!!!");
    				for(ExampleUrl example : examples)
    				{
    					if(example.getUrl() == null)
    					{
    						continue;
    					}
    					Document d = repository.constructDocument(example.getUrl());
    					System.out.println(mmdName);
    					System.out.println(d.getMetaMetadataName());
    					System.out.println(example.getUrl().toString());    					
    					Object[] arr = new Object[] {new Object[]{example.getUrl().toString() ,mmdName,d.getMetaMetadataName()}};
    		    		params.add(arr);
    				}
    			}	
    		}
  
    	return params;
    }
	
	@Test
	public void test() {
		String message = "MMD named "+expectedMmd+
				" expects url "+testUrl+") to reslove to itself.\n"+
				"Please check the selectors.";
		assertEquals(message, expectedMmd, foundMmd);
	}
}
