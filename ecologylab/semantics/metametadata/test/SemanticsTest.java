/**
 * 
 */
package ecologylab.semantics.metametadata.test;

import java.io.File;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.io.Assets;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.GeneratedMetadataTranslationScope;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.serialization.SIMPLTranslationException;

/**
 * @author andruid
 *
 */
public class SemanticsTest extends ApplicationEnvironment
{

	/**
	 * @param applicationName
	 * @throws SIMPLTranslationException
	 */
	public SemanticsTest() throws SIMPLTranslationException
	{
		super("Semantics Test");
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param st
	 */
	public void fixCodeBase(String newProjectName)
	{
		File oldCodeBaseFile	= codeBase().file();
		println("old codeBase = " + oldCodeBaseFile);
		File parentFile				= oldCodeBaseFile.getParentFile();
		File cfCodeBaseFile		= new File(parentFile, newProjectName);
		ParsedURL cfCodeBasePurl	= new ParsedURL (cfCodeBaseFile);
		setCodeBase(cfCodeBasePurl);
		println("new codeBase = " + cfCodeBasePurl);
		
		println("Old Assets Root = " + Assets.getAssetsRoot());
		File newAssetsRoot		= new File(cfCodeBaseFile, "config");
		Assets.setAssetsRoot(new ParsedURL (newAssetsRoot));
		println("New Assets Root = " + Assets.getAssetsRoot());
		println("Cache Root = " + Assets.getCacheRoot());
	}


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			SemanticsTest st	= new SemanticsTest();
			st.fixCodeBase("cf");
//			GeneratedMetadataTranslationScope.get();
			new TestInfoCollector();
			
			Image i	= new Image();
			i.setCaption("a nice caption.");
			i.setContext("A much, much longer context");
			
			i.serialize(System.out);
		
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}



}
