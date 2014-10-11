package ecologylab.bigsemantics.compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecologylab.bigsemantics.FileUtils;
import ecologylab.bigsemantics.collecting.MetaMetadataRepositoryLocator;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metametadata.FileTools;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.RepositoryOrderingByGeneration;
import ecologylab.bigsemantics.namesandnums.SemanticsNames;
import ecologylab.generic.Debug;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.SimplTypesScope.GRAPH_SWITCH;
import ecologylab.serialization.formatenums.Format;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.translators.CodeTranslationException;
import ecologylab.translators.CodeTranslator;

/**
 * 
 * @author quyin
 * 
 */
public class MetaMetadataCompiler extends Debug // ApplicationEnvironment
{
	
	public static final String	BUILTINS_CLASS_PACKAGE														= ".builtins";

	public static final String	DECLARATION_CLASS_PACKAGE													= ".declarations";

	public static final String	DECLARATION_CLASS_SUFFIX													= "Declaration";

	private static final String	META_METADATA_COMPILER_TSCOPE_NAME								= "meta-metadata-compiler-tscope";

	private static final String	META_METADATA_COMPILER_BUILTIN_DECLARATIONS_SCOPE	= "meta-metadata-compiler-builtin-declarations-scope";

	public void compile(CompilerConfig config) throws IOException, SIMPLTranslationException,
			CodeTranslationException
	{
		debug("\n\n loading repository ...\n\n");
		SimplTypesScope.enableGraphSerialization();
		MetaMetadataRepository.initializeTypes();
		MetaMetadataRepository repository = config.loadRepository();
		SimplTypesScope tscope = repository.traverseAndGenerateTranslationScope(META_METADATA_COMPILER_TSCOPE_NAME);

		CodeTranslator codeTranslator = config.getCompiler();
		
		File generatedSemanticsLocation = config.getGeneratedSemanticsLocation();
		File libraryDir = new File(generatedSemanticsLocation, "Library");
		if (libraryDir.exists() && libraryDir.isDirectory())
		{
			FileUtils.deleteDir(libraryDir);
		}
		
		// generate declaration classes and scope
		SimplTypesScope builtinDeclarationsScope = SimplTypesScope.get(META_METADATA_COMPILER_BUILTIN_DECLARATIONS_SCOPE, new Class[] {});
		String builtinPackage = null;
		for (ClassDescriptor mdCD : tscope.getClassDescriptors())
		{
			MetaMetadata definingMmd = ((MetadataClassDescriptor) mdCD).getDefiningMmd();
			if (definingMmd.isBuiltIn())
			{
				ClassDescriptor declCD = (ClassDescriptor) mdCD.clone();
				String packageName = mdCD.getDescribedClassPackageName();
				String classSimpleName = mdCD.getDescribedClassSimpleName();
				if (definingMmd.isRootMetaMetadata())
				{
					packageName += BUILTINS_CLASS_PACKAGE + DECLARATION_CLASS_PACKAGE;
					classSimpleName += DECLARATION_CLASS_SUFFIX;
				}
				else
				{
					builtinPackage = packageName; // essentially, the old package name
					packageName = packageName.replace(BUILTINS_CLASS_PACKAGE, BUILTINS_CLASS_PACKAGE + DECLARATION_CLASS_PACKAGE);
					classSimpleName += DECLARATION_CLASS_SUFFIX;
				}
				declCD.setDescribedClassPackageName(packageName);
				declCD.setDescribedClassSimpleName(classSimpleName);
				builtinDeclarationsScope.addTranslation(declCD);
			}
		}
//			compiler.translate(cd, config.getGeneratedBuiltinDeclarationsLocation(), config,
//					newPackageName, newSimpleName, GenerateAbstractClass.TRUE);
		CompilerConfig newConfig = (CompilerConfig) config.clone();
		newConfig.setLibraryTScopeClassPackage("ecologylab.bigsemantics.metadata.builtins.declarations");
		newConfig.setLibraryTScopeClassSimpleName("MetadataBuiltinDeclarationsTranslationScope");
//		newConfig.setGenerateAbstractClass(true);
		newConfig.setBuiltinDeclarationScopeName(SemanticsNames.REPOSITORY_BUILTIN_DECLARATIONS_TYPE_SCOPE);
		newConfig.getClassesExcludedFromGeneratedTScopeClass()
		    .add("ecologylab.bigsemantics.metadata.builtins.InformationComposition");
		ClassDescriptor infoCompCD = builtinDeclarationsScope.getClassDescriptorBySimpleName("InformationCompositionDeclaration");
    codeTranslator.excludeClassFromTranslation(infoCompCD);
    if (config.getGeneratedBuiltinDeclarationsLocation() != null)
  		codeTranslator.translate(config.getGeneratedBuiltinDeclarationsLocation(), builtinDeclarationsScope, newConfig);
		
		// generate normal metadata classes
		for (ClassDescriptor mdCD : tscope.getClassDescriptors())
		{
			MetaMetadata definingMmd = ((MetadataClassDescriptor) mdCD).getDefiningMmd();
			if (definingMmd.isBuiltIn())
				codeTranslator.excludeClassFromTranslation(mdCD);
		}
		debug("\n\n compiling to " + generatedSemanticsLocation + " ...\n\n");
		codeTranslator.translate(generatedSemanticsLocation, tscope, config);
			
		// generate repository file list:
		generateRepositoryFileList(config);
		
		// serialize post-inheritance repository files:
		serializePostInheritanceRepository(config.getRepositoryLocation(), repository);
		
		generateTreeVizData(config.getRepositoryLocation(), repository);

		debug("\n\n compiler finished.");
	}

  static private String REPOSITORY_FILES_LST = "repositoryFiles.lst";
	
	public void generateRepositoryFileList(CompilerConfig config)
	{
	  File repositoryLocation = config.getRepositoryLocation();
	  assert repositoryLocation != null;
	  assert repositoryLocation.exists();
	  assert repositoryLocation.isDirectory();
    debug("Repository location: " + repositoryLocation);
    
    List<String> items = getRepositoryFileItems(repositoryLocation, config.getRepositoryFormat());
    
    File repoFilesLst = new File(repositoryLocation, REPOSITORY_FILES_LST);
    PrintWriter pw;
    try
    {
      pw = new PrintWriter(repoFilesLst);
      for (String item : items)
      {
        debug("  Repository file: " + item);
        pw.println(item);
      }
      pw.close();
    }
    catch (FileNotFoundException e)
    {
      error("Cannot write to " + repoFilesLst);
    }
	}

  private List<String> getRepositoryFileItems(File repositoryLocation, Format repositoryFormat)
  {
    String repositoryPath = repositoryLocation.getAbsolutePath();
	  
    List<File> files = MetaMetadataRepositoryLocator.listRepositoryFiles(repositoryLocation,
                                                                         repositoryFormat);
    List<String> items = new ArrayList<String>();
    for (File file : files)
    {
      String item = FileTools.getRelativePath(repositoryPath, file.getAbsolutePath());
      item = item.replace('\\', '/'); // when specifying java resources use '/'
      debug("  Repository files list item: " + item);
      items.add(item);
    }
    return items;
  }
  
  static private String POST_INHERITANCE_REPOSITORY_DIR = "PostInheritanceRepository";
  
  static private String POST_INHERITANCE_REPOSITORY_FILE_NAME = "post-inheritance-repository";
  
  public static void serializePostInheritanceRepository(File repositoryLocation, MetaMetadataRepository repository)
  {
	  assert repositoryLocation != null;
	  assert repositoryLocation.exists();
	  assert repositoryLocation.isDirectory();
    Debug.debugT(MetaMetadataCompiler.class, "Repository location: " + repositoryLocation);
    
    File postInheritanceRepositoryDir = new File(repositoryLocation.getParentFile(),
                                                 POST_INHERITANCE_REPOSITORY_DIR);
    File xmlPostInheritanceRepositoryFile = null;
    File jsonPostInheritanceRepositoryFile = null;
    if (postInheritanceRepositoryDir.exists() && postInheritanceRepositoryDir.isDirectory())
    {
      xmlPostInheritanceRepositoryFile = new File(postInheritanceRepositoryDir,
                                                  POST_INHERITANCE_REPOSITORY_FILE_NAME + ".xml");
      jsonPostInheritanceRepositoryFile = new File(postInheritanceRepositoryDir,
                                                   POST_INHERITANCE_REPOSITORY_FILE_NAME + ".json");
    
      Map<StringFormat, String> strs = new HashMap<StringFormat, String>();
      serializeRepositoryIntoFormats(repository, strs);
      
      writeStringToFile(xmlPostInheritanceRepositoryFile, strs.get(StringFormat.XML));
      writeStringToFile(jsonPostInheritanceRepositoryFile, strs.get(StringFormat.JSON));
    }
  }

  public static void generateTreeVizData(File repositoryLocation, MetaMetadataRepository repository)
  {
    File treeVizDataDir = new File(repositoryLocation.getParentFile(), "OntoViz");
    File treeVizDataFile = null;
    if (treeVizDataDir.exists() && treeVizDataDir.isDirectory())
    {
      treeVizDataFile = new File(treeVizDataDir, "mmd_repo.json");
      RepositoryOrderingByGeneration ordering = new RepositoryOrderingByGeneration();
      ordering.orderMetaMetadataForInheritance(repository.getMetaMetadataCollection());
      try
      {
        SimplTypesScope.serialize(ordering.root, treeVizDataFile, Format.JSON);
      }
      catch (SIMPLTranslationException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  static void serializeRepositoryIntoFormats(MetaMetadataRepository repository,
                                             Map<StringFormat, String> strs)
  {
    SimplTypesScope.graphSwitch = GRAPH_SWITCH.ON;
    repository.traverseAndInheritMetaMetadata();
    strs.put(StringFormat.XML, trySerializeRepository(repository, StringFormat.XML));
    strs.put(StringFormat.JSON, trySerializeRepository(repository, StringFormat.JSON));
  }

  private static String trySerializeRepository(MetaMetadataRepository repository, StringFormat format)
  {
    try
    {
      return SimplTypesScope.serialize(repository, format).toString();
    }
    catch (SIMPLTranslationException e)
    {
      Debug.error(MetaMetadataCompiler.class,
                  "Cannot serialize post-inheritance repository in format " + format);
      e.printStackTrace();
    }
    return null;
  }

  private static void writeStringToFile(File file, String str)
  {
    BufferedWriter bw;
    try
    {
      bw = new BufferedWriter(new FileWriter(file));
      bw.write(str);
      bw.close();
    }
    catch (IOException e)
    {
      Debug.error(MetaMetadataCompiler.class, "Cannot write to " + file);
      e.printStackTrace();
    }
  }
  
  private static void error(String msg)
  {
    Debug.error(MetaMetadataCompiler.class, msg);
  }

	/**
	 * @param args
	 * 
	 * @throws IOException
	 * @throws SIMPLTranslationException
	 * @throws CodeTranslationException
	 */
	public static void main(String[] args) throws IOException, SIMPLTranslationException,
			CodeTranslationException
	{
		if (args.length < 2 || args.length > 3)
		{
			error("args: <target-language> <generated-semantics-location> [<generated-builtin-declarations-location>]");
			error("  - <target-language>: e.g. java or csharp (cs, c#).");
			error("  - <generated-semantics-location>: the path to the location for generated semantics.");
			error("  - <generated-builtin-declarations-location>: the path to the location for generated builtin declarations.");
			System.exit(-1);
		}

		String lang = args[0].toLowerCase();
		String semanticsLoc = args[1];
		String builtinDeclarationsLoc = args.length == 3 ? args[2] : null;

		File generatedSemanticsLocation = new File(semanticsLoc);
		File generatedBuiltinSemanticsLocation = builtinDeclarationsLoc == null ? null : new File(builtinDeclarationsLoc);
    CompilerConfig config = new CompilerConfig(lang, generatedSemanticsLocation, generatedBuiltinSemanticsLocation);
		MetaMetadataCompiler compiler = new MetaMetadataCompiler();
		compiler.compile(config);
	}

}
