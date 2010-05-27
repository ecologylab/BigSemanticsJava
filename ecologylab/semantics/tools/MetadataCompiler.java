/**
 * 
 */
package ecologylab.semantics.tools;

import java.io.File;
import java.io.IOException;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.XMLTranslationException;

/**
 * @author andruid
 * 
 */
public class MetadataCompiler extends ApplicationEnvironment
{
	private static String					importStatement;

	static final TranslationScope	META_METADATA_TRANSLATIONS	= MetaMetadataTranslationScope.get();

	public static final String		DEFAULT_REPOSITORY_DIRECTORY	= "../cf/config/semantics/metametadata/metaMetadataRepository";

	public static String getImportStatement()
	{
		return importStatement;
	}

	public MetadataCompiler(String[] args) throws XMLTranslationException
	{
		super("MetadataCompiler", META_METADATA_TRANSLATIONS, args, 1.0F);
	}

	public void compile()
	{

		compile(DEFAULT_REPOSITORY_DIRECTORY, MetadataCompilerUtils.DEFAULT_GENERATED_SEMANTICS_LOCATION);
	}

	public void compile(String mmdRepositoryDir)
	{
		compile(mmdRepositoryDir, MetadataCompilerUtils.DEFAULT_GENERATED_SEMANTICS_LOCATION);
	}

	public void compile(String mmdRepositoryDir, String generatedSemanticsLocation)
	{
		// ElementState.setUseDOMForTranslateTo(true);
		MetaMetadataRepository metaMetadataRepository;
		metaMetadataRepository = MetaMetadataRepository.load(new File(mmdRepositoryDir));
		//metaMetadataRepository.translateToXML(System.out);
		
		// for each metadata first find the list of packages in which they have to
		// be generated.
		importStatement = MetadataCompilerUtils.IMPORTS;
		for (MetaMetadata metaMetadata : metaMetadataRepository.values())
		{
			if (metaMetadata.getPackageAttribute() != null)
			{
				importStatement += "import " + metaMetadata.getPackageAttribute() + ".*;\n";
			}
		}

		// Writer for the translation scope for generated class.
		String generatedSemanticsPath = MetadataCompilerUtils.getGenerationPath(
				metaMetadataRepository.getPackageName(), generatedSemanticsLocation);
		try {
			MetadataCompilerUtils.createTranslationScopeClass(generatedSemanticsPath,
					metaMetadataRepository.packageName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// for each meta-metadata in the repository
		for (MetaMetadata metaMetadata : metaMetadataRepository.values())
		{
			// if a metadataclass has to be generated
			if (metaMetadata.isGenerateClass())
			{
				// translate it into a meta data class.
				try {
					metaMetadata.translateToMetadataClass(metaMetadataRepository.getPackageName(),
							metaMetadataRepository);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MetadataCompilerUtils.appendToTranslationScope(XMLTools
						.classNameFromElementName(metaMetadata.getName())
						+ ".class,\n");
				System.out.println('\n');
			}
		}

		// end the translationScope class
		MetadataCompilerUtils.endTranslationScopeClass();
		
	}

	public static void main(String[] args)
	{
		try
		{
			MetadataCompiler compiler = new MetadataCompiler(args);
			compiler.compile();
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
