package ecologylab.bigsemantics.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecologylab.bigsemantics.collecting.MetaMetadataRepositoryLocator;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepositoryLoader;
import ecologylab.bigsemantics.metametadata.exceptions.MetaMetadataException;
import ecologylab.generic.ReflectionTools;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.formatenums.Format;
import ecologylab.translators.CodeTranslator;
import ecologylab.translators.CodeTranslatorConfig;

/**
 * The encapsulation of compiler configurations. This class provides the default configuration,
 * which loads a XML repository from a default location and generates Java source codes to another
 * prescribed location (the ecologylabGeneratedSemantics project).
 * <p />
 * New configurations could be loaded through SIMPL.
 * 
 * @author quyin
 * 
 */
@simpl_inherit
public class CompilerConfig extends CodeTranslatorConfig
{

	public static final String												JAVA						= "java";

	public static final String												CSHARP					= "csharp";

	private static final Map<String, CodeTranslator>	compilers	= new HashMap<String, CodeTranslator>();

	static
	{
		CodeTranslator javaCompiler = new MetaMetadataJavaTranslator();
		CodeTranslator csharpCompiler = new MetaMetadataDotNetTranslator();
		
		registerCompiler(JAVA, javaCompiler);

		registerCompiler(CSHARP, csharpCompiler);
		registerCompiler("c_sharp", csharpCompiler);
		registerCompiler("cs", csharpCompiler);
		registerCompiler("c#", csharpCompiler);
	}

	/**
	 * Provide a registering mechanism for extending translators.
	 * 
	 * @param targetLanguage
	 * @param codeTranslator
	 */
	public static void registerCompiler(String targetLanguage, CodeTranslator codeTranslator)
	{
		compilers.put(targetLanguage, codeTranslator);
	}

	/**
	 * The location (directory) of the repository.
	 */
	@simpl_scalar
	@simpl_hints({ Hint.XML_LEAF })
	private File													repositoryLocation = MetaMetadataRepositoryLocator.locateRepositoryByDefaultLocations();

	/**
	 * The format in which repository is stored.
	 */
	@simpl_scalar
	@simpl_hints({ Hint.XML_LEAF })
	private Format												repositoryFormat		= Format.XML;

	/**
	 * The location (directory) of generated semantics.
	 */
	@simpl_scalar
	@simpl_hints({ Hint.XML_LEAF })
	private File													generatedSemanticsLocation;

	@simpl_scalar
	@simpl_hints({ Hint.XML_LEAF })
	private File													generatedBuiltinDeclarationsLocation;

	@simpl_scalar
	@simpl_hints({ Hint.XML_LEAF })
	private String												builtinDeclarationScopeName;
	
	@simpl_collection("class_name")
	private List<String>                  classesExcludedFromGeneratedTScopeClass;

	/**
	 * The target languange.
	 */
	@simpl_scalar
	@simpl_hints({ Hint.XML_LEAF })
	private String												targetLanguage;

	@simpl_scalar
	@simpl_hints({ Hint.XML_LEAF })
	private String												codeTranslatorClass;

	private MetaMetadataRepositoryLoader	repositoryLoader;

	private CodeTranslator								compiler;
	
	private MetaMetadataRepository				repository;

	/**
	 * Constructor for S.IM.PL.
	 */
	public CompilerConfig()
	{
		this(JAVA, new File("../ecologylabGeneratedSemantics"), new File("../ecologylabSemantics/src/ecologylab/semantics/metadata/builtins/declarations"));
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param targetLanguage
	 * @param generatedSemanticsLocation
	 */
	public CompilerConfig(String targetLanguage, File generatedSemanticsLocation, File generatedBuiltinDeclarationsLocation)
	{
		super("ecologylab.bigsemantics.generated.library", "RepositoryMetadataTranslationScope");
		this.targetLanguage = targetLanguage;
		this.generatedSemanticsLocation = generatedSemanticsLocation;
		this.generatedBuiltinDeclarationsLocation = generatedBuiltinDeclarationsLocation;
		this.classesExcludedFromGeneratedTScopeClass = new ArrayList<String>();
	}
	
	public String getTargetLanguage()
	{
		return this.targetLanguage;
	}
	
	public File getRepositoryLocation()
	{
	  return repositoryLocation;
	}
	
	public Format getRepositoryFormat()
	{
	  return repositoryFormat;
	}

	/**
	 * @return The repository loader.
	 */
	protected MetaMetadataRepositoryLoader getRepositoryLoader()
	{
		if (repositoryLoader == null)
			repositoryLoader = new MetaMetadataRepositoryLoader();
		return repositoryLoader;
	}

	/**
	 * Load repository using current configs.
	 * 
	 * @return
	 * @throws SIMPLTranslationException 
	 * @throws FileNotFoundException 
	 */
	public MetaMetadataRepository loadRepository() throws IOException, SIMPLTranslationException
	{
		if (repository == null)
			repository = getRepositoryLoader().loadFromDir(repositoryLocation, repositoryFormat);
		return repository;
	}

	/**
	 * @return The location (directory) generated semantics will be stored.
	 */
	public File getGeneratedSemanticsLocation()
	{
		return generatedSemanticsLocation;
	}
	
	public File getGeneratedBuiltinDeclarationsLocation()
	{
		return generatedBuiltinDeclarationsLocation;
	}

	/**
	 * @return The source code translator (which translates a SIMPL scope to a set of source code
	 *         files in the target language).
	 */
	public CodeTranslator getCompiler()
	{
		if (compiler == null)
		{
			compiler = compilers.get(targetLanguage);
			if (compiler == null && codeTranslatorClass != null)
			{
				try
				{
					Class<? extends CodeTranslator> TC =  (Class<? extends CodeTranslator>) Class.forName(codeTranslatorClass);
					compiler = ReflectionTools.getInstance(TC);
				}
				catch (ClassNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (compiler == null)
			{
				throw new MetaMetadataException("Unregistered or unknown target language: "
						+ targetLanguage);
			}
		}
		return compiler;
	}

	public String getBuiltinDeclarationScopeName()
	{
		return builtinDeclarationScopeName;
	}

	public void setBuiltinDeclarationScopeName(String builtinDeclarationScopeName)
	{
		this.builtinDeclarationScopeName = builtinDeclarationScopeName;
	}
	
	public List<String> getClassesExcludedFromGeneratedTScopeClass()
	{
	  return classesExcludedFromGeneratedTScopeClass;
	}

}
