package ecologylab.semantics.metametadata.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;

import ecologylab.semantics.collecting.MetaMetadataRepositoryInit;
import ecologylab.semantics.metadata.scalar.types.SemanticsTypes;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataNestedField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataScalarField;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.semantics.metametadata.NestedMetaMetadataFieldTranslationScope;
import ecologylab.serialization.ElementState.FORMAT;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;

public class TestRepositoryInJSON
{
	
	private static TranslationScope mmdTScope = null;
	
	private void translateRepositoryIntoJSON(File srcDir, File destDir)
	{
		translateRepositoryDirIntoJSON(srcDir, destDir);
		translateRepositoryDirIntoJSON(new File(srcDir, "repositorySources"), new File(destDir, "repositorySources"));
		translateRepositoryDirIntoJSON(new File(srcDir, "powerUser"), new File(destDir, "powerUser"));
	}
	
	private void translateRepositoryDirIntoJSON(File srcDir, File destDir)
	{
		if (!destDir.exists())
			destDir.mkdir();
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname)
			{
				return pathname.getName().endsWith("xml");
			}
		};
		for (File f : srcDir.listFiles(filter))
		{
			try
			{
				MetaMetadataRepository repo = (MetaMetadataRepository) mmdTScope.deserialize(f);
				String json = repo.serialize(FORMAT.JSON).toString();
				if (json != null && json.length() > 0)
				{
					File jsonRepoFile = new File(destDir, f.getName().replace((CharSequence) ".xml", (CharSequence) ".json"));
					FileWriter writer = new FileWriter(jsonRepoFile);
					writer.write(json);
					writer.close();
				}
			}
			catch (SIMPLTranslationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		TranslationScope.graphSwitch	= GRAPH_SWITCH.ON;
		MetaMetadataRepository.initializeTypes();
		new SemanticsTypes();
		
		// ********* each time, run one of the following 3 methods: *********
		
//		testLoadingAndSavingXmlRepository();
		testSavedAgainXmlRepository();
//		testConvertingRepositoryFromXmlToJson();
	}
	
	private static File destRepoDir = new File("/tmp/repository/");
	
	private static void testLoadingAndSavingXmlRepository()
	{
		// *********************************************************************************************
		// * this method uses a different translation scope
		// * (MetaMetadataCollectionFieldWithoutChildComposite instead of MetaMetadataCollectionField),
		// * but the other two methods (testSavedAgainXmlRepository() and
		// * testConvertingRepositoryFromXmlToJson()) need the original translation scope to work
		// * properly.
		// *********************************************************************************************
		
		// replace MetaMetadataCollectionField with MetaMetadataCollectionFieldChildComposite
		TranslationScope.get(NestedMetaMetadataFieldTranslationScope.NAME, new Class[] {
				MetaMetadataField.class,
				MetaMetadataScalarField.class,
				MetaMetadataCompositeField.class,
				MetaMetadataCollectionFieldWithoutChildComposite.class,
		});
		mmdTScope = MetaMetadataTranslationScope.get();
		File srcRepoDir = new File("../ecologylabSemantics/repository/");
		
		// load and save the repository again
		testLoadAndSaveXmlRepositoryDir(srcRepoDir, destRepoDir);
		testLoadAndSaveXmlRepositoryDir(new File(srcRepoDir, "repositorySources"), new File(destRepoDir, "repositorySources"));
		testLoadAndSaveXmlRepositoryDir(new File(srcRepoDir, "powerUser"), new File(destRepoDir, "powerUser"));
	}

	private static void testLoadAndSaveXmlRepositoryDir(File srcDir, File destDir)
	{
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname)
			{
				return pathname.getName().endsWith(".xml");
			}
		};
		for (File xmlFile : srcDir.listFiles(filter))
		{
			try
			{
				MetaMetadataRepository repo = (MetaMetadataRepository) mmdTScope.deserialize(xmlFile);
				repo.serialize(new File(destDir, xmlFile.getName()));
			}
			catch (SIMPLTranslationException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void testSavedAgainXmlRepository()
	{
		// use json repository for NewMmTest
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_LOCATION = destRepoDir.getAbsolutePath();
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FILE_SUFFIX = ".xml";
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FILE_LOADER = MetaMetadataRepository.XML_FILE_LOADER;
		
		tryNewMmTest();
	}

	private static void testConvertingRepositoryFromXmlToJson()
	{
		// init
		mmdTScope = MetaMetadataTranslationScope.get();
		
		// convert repository to json
		TestRepositoryInJSON trij = new TestRepositoryInJSON();
		trij.translateRepositoryIntoJSON(new File("../ecologylabSemantics/repository"), new File("../ecologylabSemantics/repositoryInJSON"));
		
		// use json repository for NewMmTest
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_LOCATION = "../ecologylabSemantics/repositoryInJSON";
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FILE_SUFFIX = ".json";
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FILE_LOADER = MetaMetadataRepository.JSON_FILE_LOADER;
		
		tryNewMmTest();
	}

	private static void tryNewMmTest()
	{
		// use a set of URLs to test extraction
		String[] testUrls = new String[] {
			"http://www.dlese.org/dds/services/ddsws1-1?verb=UserSearch&q=water+on+mars&s=0&n=10&client=ddsws10examples",
			"http://www.dlese.org/dds/services/ddsws1-0?verb=GetRecord&id=DLESE-000-000-000-001",
			"http://where.yahooapis.com/geocode?gflags=R&q=-96.28616666666667,30.604833333333332",
			"http://news.blogs.cnn.com/2011/04/14/predator-dinosaurs-may-have-been-night-hunters/?hpt=C2",
			"http://remodelista.com/products/victoria-and-albert-wessex-bath",
			"http://portal.acm.org/citation.cfm?id=1416955",
			"http://buzzlog.yahoo.com/feeds/buzzsportm.xml",
			"http://www.informaworld.com/smpp/content~db=all?content=10.1080/10447310802142243",
			"http://rss.cnn.com/rss/cnn_topstories.rss",
			"//",
		};
		NewMmTest mmTest;
		try
		{
			mmTest = new NewMmTest("NewMmTest");
			mmTest.collect(testUrls);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}

}
