/**
 * 
 */
package ecologylab.semantics.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;

/**
 * @author andruid
 * 
 */
public class MetaMetadataCompiler extends ApplicationEnvironment
{
	{
		MetaMetadataRepository.initializeTypes();
	}

	public static final String		DEFAULT_REPOSITORY_DIRECTORY	= "repository";

	public MetaMetadataCompiler(String[] args) throws SIMPLTranslationException
	{
		super("MetadataCompiler", MetaMetadataTranslationScope.get(), args, 1.0F);
	}

	public void compile()
	{

		compile(DEFAULT_REPOSITORY_DIRECTORY, MetaMetadataCompilerUtils.DEFAULT_GENERATED_SEMANTICS_LOCATION);
	}

	public static void printImports(Appendable appendable) throws IOException
	{
		List<String>			 targetList	= new ArrayList(MetaMetadataCompilerUtils.importTargets);
		Collections.sort(targetList);
		
		appendable.append('\n');
		importCollection(appendable, targetList);
		appendable.append('\n');
	}
	
	private static void importCollection(Appendable appendable, Iterable<String> collection) 
	throws IOException
	{
		for (String thatEntry : collection)
		{
			appendable.append("import ");
			appendable.append(thatEntry);
			appendable.append(";\n");
		}
	}
	
	public void compile(String mmdRepositoryDir)
	{
		compile(mmdRepositoryDir, MetaMetadataCompilerUtils.DEFAULT_GENERATED_SEMANTICS_LOCATION);
	}
	
	public void compile(String mmdRepositoryDir, String generatedSemanticsLocation)
	{
		MetaMetadataRepository metaMetadataRepository = MetaMetadataRepository.load(new File(mmdRepositoryDir));
		compile(metaMetadataRepository, generatedSemanticsLocation);
	}

	public void compile(MetaMetadataRepository metaMetadataRepository, String generatedSemanticsLocation)
	{
		// ElementState.setUseDOMForTranslateTo(true);
		//metaMetadataRepository.translateToXML(System.out);
		
		// for each metadata first find the list of packages in which they have to
		// be generated.
		for (MetaMetadata metaMetadata : metaMetadataRepository.values())
		{
			String packageAttribute = metaMetadata.getPackageAttribute();
			if (packageAttribute != null)
				MetaMetadataCompilerUtils.importTargets.add(packageAttribute + ".*");
		}

		// Writer for the translation scope for generated class.
		String generatedSemanticsPath = MetaMetadataCompilerUtils.getGenerationPath(
				metaMetadataRepository.getPackageName(), generatedSemanticsLocation);
		try {
			MetaMetadataCompilerUtils.createTranslationScopeClass(generatedSemanticsPath,
					metaMetadataRepository.packageName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// for each meta-metadata in the repository
		for (MetaMetadata metaMetadata : metaMetadataRepository.values())
		{
//			if (metaMetadata.getName() == null)
//				continue;
			
			// if a metadataclass has to be generated
			if (metaMetadata.isGenerateClass())
			{
				// translate it into a meta data class.
				try {
					metaMetadata.compileToMetadataClass(metaMetadataRepository.getPackageName(),
							metaMetadataRepository);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String packageName	= metaMetadata.getPackageAttribute();
				if (packageName == null)
					packageName				= "";
				else
					packageName				+= ".";
				MetaMetadataCompilerUtils.appendToTranslationScope(packageName + XMLTools.classNameFromElementName(metaMetadata.getName())
						+ ".class,\n");
				System.out.println('\n');
			}
		}

		// end the translationScope class
		MetaMetadataCompilerUtils.endTranslationScopeClass();
		
	}

	public static void main(String[] args)
	{
		try
		{
			MetaMetadataCompiler compiler = new MetaMetadataCompiler(args);
			compiler.compile();
		}
		catch (SIMPLTranslationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
