package ecologylab.bigsemantics.metametadata;

import java.util.ArrayList;

import ecologylab.bigsemantics.tools.MmTest;
import ecologylab.serialization.SIMPLTranslationException;

public class TestMmdExampleCoverage extends MmTest
{

	public TestMmdExampleCoverage(String appName) throws SIMPLTranslationException
	{
		super(appName);
	}

	public static void main(String args[])
	{
		TestMmdExampleCoverage speedTest;
		String mmdsWithoutExamples = "";
		String mmdsWithExamples = "";
		String justURLS = "";
		try
		{
			speedTest = new TestMmdExampleCoverage("TestMmdExampleCoverage");
			MetaMetadataRepository repository = speedTest.semanticsSessionScope.getMetaMetadataRepository();
			ArrayList<String> mmdsByName = repository.getMMNameList();
			for(String mmdName : mmdsByName)
			{
				System.out.println("MMD:"+mmdName);
				MetaMetadata mmd = repository.getMMByName(mmdName);
				ArrayList<ExampleUrl> examples = mmd.getExampleUrls();
				if(examples == null)
				{
					System.out.println("No example urls!!!");
					mmdsWithoutExamples += mmdName+",";
				}
				else
				{
					mmdsWithExamples += mmdName+",";
					for(ExampleUrl example : examples)
					{
						System.out.println(example.getUrl());
						justURLS += example.getUrl()+"\n";
					}
				}
				
			}
			
			System.out.println("=========SUMMARY=========");
			System.out.println("MMD without example URL:");
			System.out.println(mmdsWithoutExamples);
			System.out.println("\n");
			System.out.println("MMD with example URL:");
			System.out.println(mmdsWithExamples);
			System.out.println("\n");
			System.out.println("All URLs from examples:");
			System.out.println(justURLS);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}
	
}
