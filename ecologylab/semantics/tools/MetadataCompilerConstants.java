/**
 * 
 */
package ecologylab.semantics.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.appframework.types.prefs.PrefString;
import ecologylab.io.Files;
import ecologylab.semantics.library.DefaultMetadataTranslationSpace;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;

/**
 * This class has all the constants which are needed by the compiler.
 * 
 * @author amathur
 * 
 */
public class MetadataCompilerConstants
{

	private static final String DEFAULT_GENERATED_SEMANTICS_LOCATION = ".." + Files.sep + "ecologylabGeneratedSemantics";

	public static String				PACKAGE_NAME				= "package ecologylab.semantics.generated.library;\n";

	public static String				START_JAVA_DOC			= "\n/**\n";

	public static String				END_JAVA_DOC				= "\n**/ \n\n";

	public static String				COMMENT							= START_JAVA_DOC
																											+ "This is a generated code. DO NOT edit or modify it.\n @author MetadataCompiler \n"
																											+ END_JAVA_DOC;

	//FIXME -- get rid of library hard coded entries in this string!
	public static String				IMPORTS							= "\n" + MetaMetadataRepository.builtinMetadataTranslations().generateImports() + "\n  import java.util.*;\n import ecologylab.semantics.metametadata.MetaMetadata;\n  import ecologylab.net.ParsedURL;\n import ecologylab.generic.HashMapArrayList;\n import ecologylab.xml.xml_inherit;\nimport ecologylab.xml.types.element.Mappable;\nimport ecologylab.semantics.library.DefaultMetadataTranslationSpace;\n import ecologylab.semantics.library.scholarlyPublication.*;\nimport ecologylab.semantics.library.uva.*;\nimport ecologylab.xml.TranslationScope;\nimport ecologylab.xml.ElementState.xml_tag;\n import ecologylab.semantics.metadata.builtins.Document;\nimport ecologylab.semantics.metadata.builtins.Media;\n import ecologylab.semantics.metadata.builtins.Image;\nimport ecologylab.semantics.metametadata.MetaMetadataRepository;\nimport ecologylab.semantics.generated.library.*;\n";

	public static String				PACKAGE							= "package";

	public static PrintWriter		generatedTranslationScope;

	public static final HashMap	JAVA_KEY_WORDS_MAP	= new HashMap();

	static
	{
		JAVA_KEY_WORDS_MAP.put("abstract", "abstractField");
		JAVA_KEY_WORDS_MAP.put("package", "packageField");
	}

	/**
	 * TODO change this to write to file
	 * 
	 * @param comment
	 * @throws IOException
	 */
	public static void writeJavaDocComment(String comment, Appendable f) throws IOException
	{
		System.out.println(START_JAVA_DOC + "\t" + comment + END_JAVA_DOC);
		f.append(START_JAVA_DOC + "\t" + comment + END_JAVA_DOC);
	}

	/**
	 * This method returns the path where the generated files are to be placed. TODO FIX ME TO USE
	 * PREFS.
	 * 
	 * @param packageName
	 *          The package in which generated files are to be placed.
	 * @return The path.
	 */
	public static String getGenerationPath(String packageName)
	{
		PrefString relativePath = PrefString.usePrefString("metadata_generated_relative_path",
				DEFAULT_GENERATED_SEMANTICS_LOCATION);
		// String userDirProperty = System.getProperty("user.dir");
		String generationPath = relativePath.value() + Files.sep + packageName.replace('.', Files.sep) + Files.sep;
		return generationPath;
	}

	public static void main(String[] args)
	{
		System.out.println(getGenerationPath("ecologylab.semantics.generated.library"));
	}

	/**
	 * This method appends blank constructor
	 * 
	 * @throws IOException
	 */
	public static void appendBlankConstructor(Appendable appendable, String className)
			throws IOException
	{
		String comment = "Constructor";
		MetadataCompilerConstants.writeJavaDocComment(comment, appendable);
		appendable.append("public ").append(className + "()\n{\n super();\n}\n");
	}

	/**
	 * This appends a constructor with call to super.
	 */
	public static void appendConstructor(Appendable appendable, String className) throws IOException
	{
		String comment = "Constructor";
		MetadataCompilerConstants.writeJavaDocComment(comment, appendable);
		appendable.append("public ").append(className).append(
				"(MetaMetadata metaMetadata)\n{\nsuper(metaMetadata);\n}\n");
	}

	/**
	 * This function checks for JAVA keyword and if a key word is found we append "Field" string after
	 * it.
	 * 
	 * @param name
	 * @return
	 */
	public static String handleJavaKeyWord(String name)
	{
		if (JAVA_KEY_WORDS_MAP.containsKey(name))
		{
			return (String) JAVA_KEY_WORDS_MAP.get(name);
		}
		else
			return name;
	}

	/**
	 * Creates a class for writing translation scope for generated classes.
	 * 
	 * @param generationPath
	 * @throws IOException
	 */
	public static void createTranslationScopeClass(String generationPath) throws IOException
	{
		File directoryPath = PropertiesAndDirectories.createDirsAsNeeded(new File(generationPath));
		File file = new File(directoryPath, XMLTools
				.classNameFromElementName("GeneratedMetadataTranslationScope")
				+ ".java");
		FileWriter fileWriter = new FileWriter(file);
		MetadataCompilerConstants.generatedTranslationScope = new PrintWriter(fileWriter);

		// Write the package
		MetadataCompilerConstants.generatedTranslationScope
				.println(MetadataCompilerConstants.PACKAGE_NAME);

		// write java doc comment
		MetadataCompilerConstants.generatedTranslationScope.println(MetadataCompilerConstants.COMMENT);

		// Write the import statements
		MetadataCompilerConstants.generatedTranslationScope.println(MetadataCompilerConstants.IMPORTS);

		// Write java-doc comments
		MetadataCompilerConstants.writeJavaDocComment(
				"\nThis is the tranlation scope class for generated files\n.",
				MetadataCompilerConstants.generatedTranslationScope);

		// begin writing the class
		MetadataCompilerConstants.generatedTranslationScope
				.print("public class GeneratedMetadataTranslationScope extends  DefaultMetadataTranslationSpace\n{");
		MetadataCompilerConstants.generatedTranslationScope
				.print("protected static final Class TRANSLATIONS[]=\n\t{");

	}

	/**
	 * It appends the specified String to the translation scope class.
	 * 
	 * @param append
	 */
	public static void appendToTranslationScope(String append)
	{
		MetadataCompilerConstants.generatedTranslationScope.println(append);
	}

	/**
	 * This metod adds the get() method to generated tralation scope and flushes the class out.
	 */
	public static void endTranslationScopeClass()
	{
		MetadataCompilerConstants.generatedTranslationScope.print("\n};\n \n");
		MetadataCompilerConstants.generatedTranslationScope
				.print("public static TranslationScope get()\n{\n");
		MetadataCompilerConstants.generatedTranslationScope
				.print("return TranslationScope.get(\"generatedMetadataTranslations\", MetaMetadataRepository.builtinMetadataTranslations(), TRANSLATIONS);\n}\n}");

		MetadataCompilerConstants.generatedTranslationScope.flush();
	}
}
