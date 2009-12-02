/**
 * 
 */
package ecologylab.semantics.tools;

import java.io.File;
import java.io.IOException;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.XMLTranslationException;

/**
 * @author andruid
 * 
 */
public class MetadataCompiler extends ApplicationEnvironment
{
	private static String importStatement;
	
	/**
	 * @param applicationName
	 * @throws XMLTranslationException
	 */
	public MetadataCompiler(String applicationName) throws XMLTranslationException
	{
		super(applicationName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param applicationName
	 * @param translationSpace
	 * @param args
	 * @param prefsAssetVersion
	 * @throws XMLTranslationException
	 */
	public MetadataCompiler(String applicationName, TranslationScope translationSpace, String[] args,
			float prefsAssetVersion) throws XMLTranslationException
	{
		super(applicationName, translationSpace, args, prefsAssetVersion);
		// TODO Auto-generated constructor stub
	}

	static final TranslationScope	META_METADATA_TRANSLATIONS	= MetaMetadataTranslationScope.get();

	/**
	 * @param applicationName
	 * @param args
	 * @throws XMLTranslationException
	 */
	public MetadataCompiler(String[] args) throws XMLTranslationException
	{
		super("MetadataCompiler", META_METADATA_TRANSLATIONS, args, 1.0F);

		String patternXMLFilepath = "../cf/config/semantics/metametadata/metaMetadataRepository.xml";

		// ElementState.setUseDOMForTranslateTo(true);
		MetaMetadataRepository metaMetadataRepository;
		try
		{
			metaMetadataRepository = MetaMetadataRepository.load(new File(patternXMLFilepath));
			// metaMetadataRepository.translateToXML(System.out);

			// for each metadata first find the list of packages in which they have to
			// be generated.
			importStatement = MetadataCompilerUtils.IMPORTS;
			for (MetaMetadata metaMetadata : metaMetadataRepository.values())
			{
				if(metaMetadata.getPackageAttribute()!=null)
				{
					importStatement += "import "+metaMetadata.getPackageAttribute()+".*;\n";
				}
			}
			
			// Writer for the translation scope for generated class.
			MetadataCompilerUtils.createTranslationScopeClass(MetadataCompilerUtils.getGenerationPath(metaMetadataRepository.getPackageName()));

			// for each meta-metadata in the repository
			for (MetaMetadata metaMetadata : metaMetadataRepository.values())
			{
				// if a metadataclass has to be generated
				if (metaMetadata.isGenerateClass())
				{
					// translate it into a meta data class.
					metaMetadata.translateToMetadataClass(metaMetadataRepository.getPackageName(), metaMetadataRepository);
					MetadataCompilerUtils.appendToTranslationScope(XMLTools
							.classNameFromElementName(metaMetadata.getName())
							+ ".class,\n");
					System.out.println('\n');
				}
			}

			// end the translationScope class
			MetadataCompilerUtils.endTranslationScopeClass();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param baseClass
	 * @param applicationName
	 * @param args
	 * @throws XMLTranslationException
	 */
	public MetadataCompiler(Class baseClass, String applicationName, String[] args)
			throws XMLTranslationException
	{
		super(baseClass, applicationName, args);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param baseClass
	 * @param applicationName
	 * @param translationSpace
	 * @param args
	 * @param prefsAssetVersion
	 * @throws XMLTranslationException
	 */
	public MetadataCompiler(Class baseClass, String applicationName,
			TranslationScope translationSpace, String[] args, float prefsAssetVersion)
			throws XMLTranslationException
	{
		super(baseClass, applicationName, translationSpace, args, prefsAssetVersion);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			new MetadataCompiler(args);
		}
		catch (XMLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @return the importStatement
	 */
	public static String getImportStatement()
	{
		return importStatement;
	}

}
