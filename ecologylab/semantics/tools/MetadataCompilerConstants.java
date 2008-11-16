/**
 * 
 */
package ecologylab.semantics.tools;

import java.io.IOException;
import java.util.HashMap;

import ecologylab.appframework.ApplicationEnvironment;
import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.appframework.types.prefs.PrefString;
import ecologylab.appframework.types.prefs.gui.PrefsEditor;
import ecologylab.xml.XMLTranslationException;

/**
 * This class has all the constants which are needed by the compiler.
 * 
 * @author amathur
 * 
 */
public class MetadataCompilerConstants
{

	public static String				PACKAGE_NAME				= "package ecologylab.semantic.generated.library;\n";

	public static String				START_JAVA_DOC			= "\n/**\n";

	public static String				END_JAVA_DOC				= "\n**/ \n\n";

	public static String				COMMENT							= START_JAVA_DOC
																											+ "This is a generated code. DO NOT edit or modify it.\n @author MetadataCompiler \n"
																											+ END_JAVA_DOC;

	public static String				IMPORTS							= "\n import ecologylab.semantics.library.scalar.*; \nimport ecologylab.semantics.metadata.*;\n  import java.util.*;\n import ecologylab.semantics.metametadata.MetaMetadata;\n  import ecologylab.net.ParsedURL;\n import ecologylab.generic.HashMapArrayList;\n import ecologylab.semantics.generated.library.*;\n";

	public static String				PACKAGE							= "package";

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
	 * This method returns the path where the generated files are to be placed. 
	 * TODO FIX ME TO USE PREFS.
	 * @param packageName
	 *          The package in which generated files are to be placed.
	 * @return The path.
	 */
	public static String getGenerationPath(String packageName)
	{
		PrefString relativePath = PrefString.usePrefString("metadata_generated_relative_path",
				"..\\ecologylabGeneratedSemantics");
		// String userDirProperty = System.getProperty("user.dir");
		String generationPath = relativePath.value() + "\\" + packageName.replace('.', '\\') + "\\";
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
		appendable.append("public ").append(className + "()\n{\n}\n");
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
}
