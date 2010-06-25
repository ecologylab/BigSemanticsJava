package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.semantics.metadata.DocumentParserTagNames;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.textformat.NamedStyle;
import ecologylab.xml.ClassDescriptor;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldDescriptor;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTools;
import ecologylab.xml.XMLTranslationException;
import ecologylab.xml.xml_inherit;
import ecologylab.xml.ElementState.xml_attribute;
import ecologylab.xml.ElementState.xml_tag;
import ecologylab.xml.types.element.Mappable;
import ecologylab.xml.types.scalar.ScalarType;

/**
 * 
 * @author damaraju
 * 
 */
@xml_inherit
public abstract class MetaMetadataField extends ElementState implements Mappable<String>, PackageSpecifier,
		Iterable<MetaMetadataField>
{
	MetadataFieldDescriptor												metadataFieldDescriptor;
	
	/**
	 * Name of the metadata field.
	 */
	@xml_attribute
	protected String															name;

	@xml_tag("extends")
	@xml_attribute
	protected String															extendsAttribute;

	/**
	 * true if this field should not be displayed in interactive in-context metadata
	 */
	@xml_attribute
	protected boolean															hide;

	/**
	 * If true the field is shown even if its null or empty.
	 */
	@xml_attribute
	protected boolean															alwaysShow;

	@xml_attribute
	protected String															style;
	
	/**
	 * Specifies the order in which a field is displayed in relation to other fields.
	 */
	@xml_attribute
	protected float																layer;

	/**
	 * XPath expression used to extract this field.
	 */
	@xml_attribute
	protected String															xpath;

	/**
	 * Another field name that this field navigates to (e.g. from a label in in-context metadata)
	 */
	@xml_attribute
	protected String															navigatesTo;

	/**
	 * This MetaMetadataField shadows another field, so it is to be displayed instead of the other.It
	 * is kind of over-riding a field.
	 */
	@xml_attribute
	protected String															shadows;
	
	/**
	 * The label to be used when visualizing this field. Name is used by default. This overrides name.
	 */
	@xml_attribute
	protected String															label;

	// FIXME -- talk to bharat, eliminate this declaration
	// no idea what this is for....added while parsing for acmportal
	/**
	 * This is used to specify the prefix string which is to be stripped off.
	 */
	@xml_attribute
	protected String															stringPrefix;

	/*
	 * @xml_attribute protected boolean isList;
	 */

	/*
	 * @xml_attribute protected boolean isMap;
	 */

	@xml_attribute
	protected boolean															isFacet;

	@xml_attribute
	protected boolean															ignoreInTermVector;

	@xml_attribute
	protected String															comment;

	/**
	 * Enables hand coding a few Metadata classes, but still providing MetaMetadata to control
	 * operations on them.
	 */
	@xml_attribute
	protected boolean															dontCompile;
	
	@xml_attribute
	protected String															key;

	/**
	 * The regular expression
	 */
	@xml_attribute
	protected String																			textRegex;

	/**
	 * The string used to replace the match.
	 */
	@xml_attribute
	protected String																			matchReplacement;
	
	/**
	 * Context node for xpath based extarction rules for this field.
	 * Default value is document root.
	 */
	@xml_attribute
	protected String																			contextNode;

	@xml_attribute 
	protected String																			tag;
	
	@xml_attribute
	protected boolean																			ignoreExtractionError;

	@xml_map
	@xml_classes({
		MetaMetadataField.class,
		MetaMetadataScalarField.class,
		MetaMetadataNestedField.class,
		MetaMetadataCollectionField.class,
		})
	@xml_nowrap
	protected HashMapArrayList<String, MetaMetadataField>	kids;
	
	HashSet<String>																				nonDisplayedFieldNames;
	
	File																									file;
	
	private boolean																				fieldsSortedForDisplay = false;

	HashMap<String, String>											childPackagesMap	= new HashMap<String, String>(2);

	private static ArrayList<MetaMetadataField>	EMPTY_COLLECTION	= new ArrayList<MetaMetadataField>(
																																		0);

	public static Iterator<MetaMetadataField>		EMPTY_ITERATOR		= EMPTY_COLLECTION.iterator();
	
	private static LayerComparator							LAYER_COMPARATOR 	= new LayerComparator();
	
	/*************These 2 variables are needed fo inheritence implementation*********************/
	/**
	 * Holds the super class of the meta-metadata to which this field belongs
	 */
	private String extendsField;
	
	/**
	 * The Meta-Metadata repository object.
	 */
	private MetaMetadataRepository mmdRepository;
	

	protected boolean						inheritMetaMetadataFinished = false;
	/**************************************************************************************/

	public MetaMetadataField()
	{

	}

	public MetaMetadataField(String name, ScalarType metadataType,
			HashMapArrayList<String, MetaMetadataField> set)
	{
		this.name = name;
		// this.metadataType = metadataType;
		this.kids = set;
	}

	protected MetaMetadataField(MetaMetadataField copy, String name)
	{
		this();
		this.name 							= name;
		this.tag								= copy.tag;
		this.extendsField				= copy.extendsField;
		this.kids 	= copy.kids;
		
		//TODO -- do we need to propagate more fields here?
		
//		this.childType					= copy.childType;
//		this.childTag						= copy.childTag;
//		this.noWrap							= copy.noWrap;
	}

	public String packageName()
	{
		String result = "";
		if (result != null)
			return result;
		ElementState parent = parent();
		if ((parent != null) && (parent instanceof PackageSpecifier))
		{
			return ((PackageSpecifier) parent).packageName();
		}
		else
			return null;
	}

	// TODO import paths

	// TODO track generated classes for TranslationScope declaration

	public void translateToMetadataClass(String packageName, Appendable appendable,int pass,boolean appendedToTranslastionScope)
			throws XMLTranslationException, IOException
	{
		doAppending(appendable, pass);

		// new java class has to be written
		if (isNewClass())
		{
			//FIXME -- call the regular routine for generating a class declaration!!!!!!!!!!!!!! code should not be duplicated
			// getting the generation path for the java class.
			String generationPath = MetadataCompilerUtils.getGenerationPath(packageName);

			// the name of the java class.
			String javaClassName = generateNewClassName();

			// if this class implements any Interface it will contain that.
			String implementDecl = "";

			// if meta-metadata field is of type map we need to implement Mappable interface
			/*
			 * if(isMap) { // find the key type by iterating over all the child metadata field. ScalarType
			 * type=this.childMetaMetadata.get(this.key).scalarType; String fieldTypeName =
			 * type.fieldTypeName(); if (fieldTypeName.equals("int")) { // HACK FOR METADATAINTEGER
			 * fieldTypeName = "Integer"; }
			 * implementDecl=implementDecl+"\timplements Mappable<"+fieldTypeName+">\n";
			 * 
			 * }
			 */

			// file writer.
			File directoryPath = PropertiesAndDirectories.createDirsAsNeeded(new File(generationPath));
			File f = new File(directoryPath, XMLTools.classNameFromElementName(javaClassName) + ".java");
			FileWriter fileWriter = new FileWriter(f);
			PrintWriter p = new PrintWriter(fileWriter);

			// writing the package declaration
			p.println(MetadataCompilerUtils.PACKAGE + " " + packageName + ";");

			// writing the imports
//			p.println(MetadataCompiler.getImportStatement());
			MetadataCompiler.printImports(p);

			// write xml_inherit
			p.println("@xml_inherit");
//			p.println("@xml_tag(\""+collectionChildType+"\")");
			p.println(getTagDecl());

			// start of class definition
			p.println("public class " + XMLTools.classNameFromElementName(javaClassName)
					+ " extends Metadata" + implementDecl + "{\n");

			// loop to write the class definition.
			for (int i = 0; i < kids.size(); i++)
			{
				// translate the each meta-metadata field into class.
				MetaMetadataField cField = kids.get(i);
				cField.setExtendsField(extendsField);
				cField.setMmdRepository(mmdRepository);
				cField.translateToMetadataClass(packageName, p,MetadataCompilerUtils.GENERATE_FIELDS_PASS,false);
			}
			
		// write the constructors
			MetadataCompilerUtils.appendBlankConstructor(p, XMLTools
					.classNameFromElementName(javaClassName));
			MetadataCompilerUtils.appendConstructor(p, XMLTools
					.classNameFromElementName(javaClassName));
			
			for (int i = 0; i < kids.size(); i++)
			{
				// translate the each meta-metadata field into class.
				MetaMetadataField cField = kids.get(i);
				cField.setExtendsField(extendsField);
				cField.setMmdRepository(mmdRepository);
				cField.translateToMetadataClass(packageName, p,MetadataCompilerUtils.GENERATE_METHODS_PASS,true);
			}

			// if this is a Map we have to implement the key() method.
			/*
			 * if(isMap) { appendKeyMethod(p); }
			 */
			// ending the class.
			p.println("}");
			p.flush();
						
			if(!appendedToTranslastionScope)
			{
				// append this class to generated translation scope
				MetadataCompilerUtils.appendToTranslationScope(XMLTools
						.classNameFromElementName(javaClassName)
						+ ".class,\n");
			}	
		}
	}

	protected String generateNewClassName()
	{
		String javaClassName = null;
		
		if (this instanceof MetaMetadataCollectionField)
		{
			// we will generate a class of the name collectionChildType.
			javaClassName = ((MetaMetadataCollectionField)this).childType;
		}
		else // this instanceof MetaMetadataNestedField
		{
			javaClassName = ((MetaMetadataNestedField)this).getTypeOrName();
		}
		
		return javaClassName;
	}

	abstract public boolean isNewClass();

	protected void appendImport(Appendable appendable, String importDecl) throws IOException
	{
		appendable.append("import ").append(importDecl).append(';').append('\n');
	}

	/**
	 * Append an @xml_nested declaration to appendable, using the name of this to directly form both
	 * the class name and the field name.
	 * 
	 * @param appendable
	 * @throws IOException
	 */
	private void appendNested(Appendable appendable) throws IOException
	{
		String elementName = getName();
		appendNested(appendable, "", XMLTools.classNameFromElementName(elementName), XMLTools
				.fieldNameFromElementName(elementName));
	}

	/**
	 * 
	 * public void hwSetQueryMetadata(MetadataString query)
{
    if (this.query != null && this.query.getValue() != null && hasTermVector())
        termVector().remove(this.query.termVector());
    this.query    = query;
    rebuildCompositeTermVector();
}
	 * @param appendable
	 * @param fieldName
	 * @param string
	 * @throws IOException 
	 */
	protected void appendDirectHWSetMethod(Appendable appendable, String fieldName, String fieldTypeName) throws IOException
	{
		String comment = "Heavy Weight Direct setter method for "+fieldName;
		
	// write the java doc comment
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);
		
		// first line
		 appendable.append("public void hwSet"+ XMLTools.javaNameFromElementName(fieldName, true)+"Metadata("+fieldTypeName+" "+fieldName+")\n{");
		 
		 // second line
		 appendable.append("\t if(this."+fieldName+"!=null && this."+fieldName+".getValue()!=null && hasTermVector())\n");
		 
		 //third line
		 appendable.append("\t\t termVector().remove(this."+fieldName+".termVector());\n");
		 
		 //fourth line
		 appendable.append("\t this."+fieldName+" = "+fieldName+";\n");
		 
		 //last line
		 appendable.append("\trebuildCompositeTermVector();\n}");
	}

	/**
	 * public void setQueryMetadata(MetadataString query)
{
    this.query    = query;
}
	 * @param appendable
	 * @param fieldName
	 * @param fieldTypeName
	 * @throws IOException 
	 */
	protected void appendDirectSetMethod(Appendable appendable, String fieldName, String fieldTypeName) throws IOException
	{
		String comment =" Sets the "+fieldName+" directly";
		
		// write the java doc comment
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);
		
		// first line
		appendable.append("public void set"+ XMLTools.javaNameFromElementName(fieldName, true)+"Metadata("+fieldTypeName+" "+fieldName+")\n{");
		
		// second line
		appendable.append("\tthis."+fieldName+" = "+fieldName+";\n}");
		
	}

	/**
	 * public void appendAnchorText(String anchorText) { this.anchorText().setValue(anchorText); }
	 * 
	 * @param appendable
	 * @param fieldName
	 * @throws IOException
	 */
	protected void appendAppendMethod(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = " Appends the value to the field " + fieldName;

		// javadoc comment
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void append" + XMLTools.javaNameFromElementName(fieldName, true)
				+ "( " + fieldType + " " + fieldName + " )\n{\n");

		// second line
		appendable.append("this." + fieldName + "().setValue(" + fieldName + ");\n}\n");

	}

	/**
	 * this.anchorText().setValue(anchorText); rebuildCompositeTermVector();
	 * 
	 * @param appendable
	 * @param fieldName
	 * @throws IOException
	 */
	protected void appendHWAppendMethod(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "The heavy weight Append method for field " + fieldName;
		// write java doc
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void hwAppend" + XMLTools.javaNameFromElementName(fieldName, true)
				+ "( " + fieldType + " " + fieldName + " )\n{\n");

		// second line
		appendable.append("this." + fieldName + "().setValue(" + fieldName + ");\n");

		// third line
		appendable.append("rebuildCompositeTermVector();\n }");
	}

	/**
	 * This method will generate the getter for the field. public String getTitle() { return
	 * title().getValue(); }
	 */
	protected void appendGetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Gets the value of the field " + fieldName;
		// write java doc
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public " + fieldType + " get"
				+ XMLTools.javaNameFromElementName(fieldName, true) + "(){\n");

		// second line
		appendable.append("return " + fieldName + "().getValue();\n}\n");

	}

	/**
	 * This method will generate setter for the field. public void setTitle(String title) {
	 * this.title().setValue(title); }
	 */
	protected void appendSetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Sets the value of the field " + fieldName;
		// write java doc
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void set" + XMLTools.javaNameFromElementName(fieldName, true) + "( "
				+ fieldType + " " + fieldName + " )\n{\n");

		// second line
		appendable.append("this." + fieldName + "().setValue(" + fieldName + ");\n}\n");
	}

	/**
	 * public void hwSetTitle(String title) { this.setTitle(title); rebuildCompositeTermVector(); }
	 */
	protected void appendHWSetter(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "The heavy weight setter method for field " + fieldName;
		// write java doc
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void hwSet" + XMLTools.javaNameFromElementName(fieldName, true)
				+ "( " + fieldType + " " + fieldName + " )\n{\n");

		// second line
		appendable.append("this." + fieldName + "().setValue(" + fieldName + ");\n");

		// third line
		appendable.append("rebuildCompositeTermVector();\n }");
	}

	/**
	 * MetadataString title() { MetadataString result = this.title; if(result == null) { result = new
	 * MetadataString(); this.title = result; } return result; }
	 * 
	 * @throws IOException
	 */
	protected void appendLazyEvaluationMethod(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Lazy Evaluation for " + fieldName;

		String returnType = fieldType;

		// write comment for this method
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line . Start of method name
		appendable.append("public ").append(returnType).append("\t").append(fieldName).append("()\n{\n");

		// second line. Declaration of result variable.
		appendable.append(returnType).append("\t").append("result\t=this.").append(fieldName).append(
				";\n");

		// third line . Start of if statement
		appendable.append("if(result == null)\n{\n");

		// fourth line. creation of new result object.
		appendable.append("result = new ").append(returnType).append("();\n");

		// fifth line . end of if statement
		appendable.append("this.").append(fieldName).append("\t=\t result;\n}\n");

		// sixth line. return statement and end of method.
		appendable.append("return result;\n}\n");

	}

	/**
	 * Appends the nested field.
	 * 
	 * @param appendable
	 *          The appendable to which the field has to be appended
	 * @param classNamePrefix
	 * @param className
	 *          The type of the field
	 * @param fieldName
	 *          The name of the field.
	 * @throws IOException
	 */
	private void appendNested(Appendable appendable, String classNamePrefix, String className,
			String fieldName) throws IOException
	{
		appendMetalanguageDecl(appendable, getTagDecl() + " @xml_nested",
				classNamePrefix, className, fieldName);
	}
	
	/**
	 * Appends scalar field with @xml_leaf annotation
	 * 
	 * @param appendable
	 * @param classNamePrefix
	 * @param className
	 * @param fieldName
	 * @throws IOException
	 */
	protected void appendLeaf(Appendable appendable, String classNamePrefix, String className,
			String fieldName) throws IOException
	{
		if ("int".equals(className))
		{
			// HACK FOR METADATAINTEGER
			className = "Integer";
		}
		appendMetalanguageDecl(appendable, getTagDecl() + " @xml_leaf",
				classNamePrefix, className, fieldName);
	}

	/**
	 * public void setAuthors(HashMapArrayList<String, Author> authors) { this.authorNames = authors;
	 * }
	 * 
	 * @param appendable
	 * @param fieldName
	 * @param fieldType
	 */
	protected void appendSetterForCollection(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Set the value of field " + fieldName;
		// write Java doc
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// write first line
		appendable.append("public void set" + XMLTools.javaNameFromElementName(fieldName, true) + "( "
				+ fieldType + " " + fieldName + " )\n{\n");
		appendable.append("this." + fieldName + " = " + fieldName + " ;\n}\n");
	}

	protected void appendGetterForCollection(Appendable appendable, String fieldName, String fieldType)
			throws IOException
	{
		String comment = "Get the value of field " + fieldName;
		// write Java doc
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// write first line
		appendable.append("public "+fieldType+" get" + XMLTools.javaNameFromElementName(fieldName, true) + "(){\n");
		appendable.append("return this." + fieldName+ ";\n}\n");
	}

	private void appendMetalanguageDecl(Appendable appendable, String metalanguage,
			String classNamePrefix, String className, String fieldName) throws IOException
	{
		appendMetalanguageDecl(appendable, metalanguage, classNamePrefix, className, "", fieldName);
	}

	protected void appendMetalanguageDecl(Appendable appendable, String metalanguage,
			String classNamePrefix, String className, String classNameSuffix, String fieldName)
			throws IOException
	{
		appendable.append('\t').append(metalanguage).append(' ').append(classNamePrefix).append(
				className).append(classNameSuffix).append('\t');
		appendable.append(fieldName).append(';').append('\n');
	}

	public static void main(String args[]) throws XMLTranslationException
	{

	}

	public int size()
	{
		return kids == null ? 0 : kids.size();
	}

	public String getName()
	{
		return name;
	}

	public String key()
	{
		return name;
	}

	public MetaMetadataField lookupChild(String name)
	{
		return kids.get(name);
	}

	public MetaMetadataField lookupChild(MetadataFieldDescriptor metadataFieldDescriptor)
	{
		return lookupChild(XMLTools.getXmlTagName(metadataFieldDescriptor.getFieldName(), null));
	}

	public String getXpath()
	{
		return xpath;
	}

	/*
	 * public boolean isList() { return isList; }
	 * 
	 * public boolean isMap() { return isMap; }
	 */
	public String getStringPrefix()
	{
		return stringPrefix;
	}

	/**
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * @param key
	 *          the key to set
	 */
	public void setKey(String key)
	{
		this.key = key;
	}

	public boolean isHide()
	{
		return hide;
	}

	protected Collection<String> importPackages()
	{
		Collection<String> result = null;
		if ((kids == null) || (kids.size() == 0))
		{
			result = new ArrayList<String>();
			result.add(packageName());
		}

		return result;
	}

	public boolean isIgnoreInTermVector()
	{
		return ignoreInTermVector;
	}
	
	/**
	 * Class of the Metadata object that corresponds to this.
	 * Non-null for nested and collection fields.
	 * Null for scalar fields.
	 */
	private Class<? extends Metadata>											metadataClass;
	

	/**
	 * Class descriptor for the Metadata object that corresponds to this.
	 * Non-null for nested and collection fields.
	 * Null for scalar fields.
	 */
	protected MetadataClassDescriptor											metadataClassDescriptor;

	boolean getClassAndBindDescriptors(TranslationScope metadataTScope)
	{
		Class<? extends Metadata> metadataClass = getMetadataClass(metadataTScope);
		if (metadataClass == null)
		{
//			error(metaMetadata + "\tCan't resolve in TranslationScope " + metadataTScope);
			return false;
		}
		//
		bindClassDescriptor(metadataClass, metadataTScope);
		return true;
	}
	
	/**
	 * Obtain a map of FieldDescriptors for this class, with the field names as key, but with the mixins field removed.
	 * Use lazy evaluation, caching the result by class name.
	 * @param metadataTScope TODO
	 * 
	 * @return	A map of FieldDescriptors, with the field names as key, but with the mixins field removed.
	 */
	final void bindClassDescriptor(Class<? extends Metadata> metadataClass, TranslationScope metadataTScope)
	{
		MetadataClassDescriptor metadataClassDescriptor = this.metadataClassDescriptor;
		if (metadataClassDescriptor == null)
		{
			synchronized (this)
			{
				metadataClassDescriptor = this.metadataClassDescriptor;
				if (metadataClassDescriptor == null)
				{
					metadataClassDescriptor = (MetadataClassDescriptor) ClassDescriptor.getClassDescriptor(metadataClass);
					bindMetadataFieldDescriptors(metadataTScope, metadataClassDescriptor);
					this.metadataClassDescriptor	= metadataClassDescriptor;
				}
			}
		}
	}
	
	HashSet<String>	nonDisplayedFieldNames()
	{
		HashSet<String>	result				= this.nonDisplayedFieldNames;
		if (result == null)
		{
			result							 				= new HashSet<String>();
			this.nonDisplayedFieldNames	= result;
		}
		return result;
	}
	public int numNonDisplayedFields()
	{
		return nonDisplayedFieldNames == null ? 0 : nonDisplayedFieldNames.size();
	}
	public boolean isChildFieldDisplayed(String childName)
	{
		return nonDisplayedFieldNames == null ? true : !nonDisplayedFieldNames.contains(childName);
	}
	/**
	 * Compute the map of FieldDescriptors for this class, with the field names as key, but with the mixins field removed.
	 * @param metadataTScope TODO
	 * @param metadataClassDescriptor TODO
	 * 
	 * @return	A map of FieldDescriptors, with the field names as key, but with the mixins field removed.
	 */
	protected final void bindMetadataFieldDescriptors(TranslationScope metadataTScope, MetadataClassDescriptor metadataClassDescriptor)
	{
		for (MetaMetadataField thatChild : kids)
		{
			thatChild.bindMetadataFieldDescriptor(metadataTScope, metadataClassDescriptor);
			
			HashSet<String> nonDisplayedFieldNames = nonDisplayedFieldNames();
			if (thatChild.hide)
				nonDisplayedFieldNames.add(thatChild.name);
			if (thatChild.shadows != null)
				nonDisplayedFieldNames.add(thatChild.shadows);
			
			// recursive descent
			if (thatChild.hasChildren())
				thatChild.getClassAndBindDescriptors(metadataTScope);
		}
//		for (FieldDescriptor fieldDescriptor : allFieldDescriptorsByFieldName.values())
//		{
//			String tagName	= fieldDescriptor.getTagName();
//			result.put(tagName, (MetadataFieldDescriptor) fieldDescriptor);
//		}
	}
	
	void bindMetadataFieldDescriptor(TranslationScope metadataTScope, MetadataClassDescriptor metadataClassDescriptor)
	{
		String tagName	= this.resolveTag(); //TODO -- is this the correct tag?
		MetadataFieldDescriptor metadataFieldDescriptor	= (MetadataFieldDescriptor) metadataClassDescriptor.getFieldDescriptorByTag(tagName, metadataTScope);
		if (metadataFieldDescriptor != null)
		{
			// if we don't have a field, then this is a wrapped collection, so we need to get the wrapped field descriptor
			if (metadataFieldDescriptor.getField() == null)
				metadataFieldDescriptor = (MetadataFieldDescriptor) metadataFieldDescriptor.getWrappedFD();
			
			this.setMetadataFieldDescriptor(metadataFieldDescriptor);
		}
		else
		{
			warning("Ignoring <" + tagName + "> because no corresponding MetadataFieldDescriptor can be found.");
		}

	}

	
	/**
	 * Lookup the Metadata class object that corresponds to tag_name, type, or extends attribute depending on which exist.
	 * <p>
	 * This method will only be called on composite fields, not scalar fields.
	 * 
	 * @return
	 */
	public Class<? extends Metadata> getMetadataClass(TranslationScope ts)
	{
		Class<? extends Metadata> result = this.metadataClass;
		
		if (result == null)
		{
			String tagForTranslationScope 	= getTagForTranslationScope();
			result													= (Class<? extends Metadata>) ts.getClassByTag(tagForTranslationScope);
			if (result == null)
			{
				if (this instanceof MetaMetadataNestedField)
					result 												= (Class<? extends Metadata>) ts.getClassByTag(((MetaMetadataNestedField)this).getTypeOrName());
				if (result == null)
				{
					// there is no class for this tag we can use class of meta-metadata it extends
					result 												= (Class<? extends Metadata>) ts.getClassByTag(extendsAttribute);
				}
			}
			if (result != null)
				this.metadataClass						= result;
			else
				ts.error("Can't resolve: " + this + " using " + tagForTranslationScope);
		}
		return result;
	}

	public Class<? extends Metadata> getMetadataClass()
	{
		return metadataClass;
	}

	
	/**
	 * Hook overrided by MetaMetadata class
	 * @param inheritedMetaMetadata
	 */
	protected void inheritSemanticActionsFromMM(MetaMetadata inheritedMetaMetadata)
	{
		//MetaMetadataFields don't have semantic actions.
	}

	/**
	 * Add a child field from a super class into the representation for this. Unless it should be
	 * shadowed, in which case ignore.
	 * 
	 * @param fieldToInheritFrom
	 */
	void inheritForField(MetaMetadataField fieldToInheritFrom)
	{
		String fieldName = fieldToInheritFrom.getName();
		// this is for the case when meta_metadata has no meta_metadata fields of its own. It just inherits from super class.
		if (kids == null)
			kids = new HashMapArrayList<String, MetaMetadataField>();
		
		// *do not* override fields in here with fields from super classes.
		MetaMetadataField fieldToInheritTo = kids.get(fieldName);
		if (fieldToInheritTo == null)
		{
			kids.put(fieldName, fieldToInheritFrom);
			fieldToInheritTo = fieldToInheritFrom;
		}
		else
		{
			fieldToInheritTo.inheritNonDefaultAttributes(fieldToInheritFrom);
		}
		
		if (fieldToInheritFrom.kids != null)
		{
			for(MetaMetadataField grandChildMetaMetadataField : fieldToInheritFrom.kids)
			{
				fieldToInheritTo.inheritForField(grandChildMetaMetadataField);
			}
		}
			
	}

	protected void inheritNonDefaultAttributes(MetaMetadataField inheritFrom)
	{
		ClassDescriptor<?, ? extends FieldDescriptor> classDescriptor	= classDescriptor();
		
		for (FieldDescriptor fieldDescriptor : classDescriptor)
		{
			ScalarType scalarType = fieldDescriptor.getScalarType();
			try
			{
				if(scalarType != null && scalarType.isDefaultValue(fieldDescriptor.getField(), this) && !scalarType.isDefaultValue(fieldDescriptor.getField(), inheritFrom))
				{
					Object value = fieldDescriptor.getField().get(inheritFrom);
					fieldDescriptor.setField(this, value);
					debug("inherit\t" + this.getName() + "." + fieldDescriptor.getFieldName() + "\t= " + value);
				}
			}
			catch (IllegalArgumentException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean hasChildren()
	{
		return kids != null && kids.size() > 0;
	}

	/**
	 * @param childMetaMetadata
	 *          the childMetaMetadata to set
	 */
	public void setChildMetaMetadata(HashMapArrayList<String, MetaMetadataField> childMetaMetadata)
	{
		this.kids = childMetaMetadata;
	}

	public Iterator<MetaMetadataField> iterator()
	{
		return (kids != null) ? kids.iterator() : EMPTY_ITERATOR;
	}
	
	public void sortForDisplay()
	{
		if (!fieldsSortedForDisplay)
		{
			Collections.sort((ArrayList<MetaMetadataField>) kids.values(), LAYER_COMPARATOR);
			fieldsSortedForDisplay = true;
		}
	}

	public boolean isAlwaysShow()
	{
		return alwaysShow;
	}

	public String shadows()
	{
		return shadows;
	}

	public String getNavigatesTo()
	{
		return navigatesTo;
	}

	/**
	 * @return the childMetaMetadata
	 */
	public HashMapArrayList<String, MetaMetadataField> getChildMetaMetadata()
	{
		return kids;
	}

	public MetaMetadataRepository metaMetadataRepository()
	{

		ElementState currentNode = this;
		while (currentNode.parent() != null)
		{
//			if (currentNodeParent instanceof MetaMetadataRepository)
//				return (MetaMetadataRepository) currentNodeParent;
//			
			currentNode = currentNode.parent();
		}
		try
		{
			return (MetaMetadataRepository) currentNode;
		}
		catch (ClassCastException e)
		{
			error("Root node of MetaMetadata Repository XML DOM is not of type MetaMetadataRepository!");
			return null;
		}
	}

	public NamedStyle lookupStyle()
	{
		return (style != null) ? metaMetadataRepository().lookupStyle(style) : metaMetadataRepository().getDefaultStyle();
	}

	/**
	 * @return the regularExpression
	 */
	public String getRegularExpression()
	{
		return textRegex;
	}

	/**
	 * @param regularExpression
	 *          the regularExpression to set
	 */
	public void setRegularExpression(String regularExpression)
	{
		this.textRegex = regularExpression;
	}

	/**
	 * @return the replacementString
	 */
	public String getReplacementString()
	{
		return matchReplacement;
	}

	/**
	 * @param replacementString
	 *          the replacementString to set
	 */
	public void setReplacementString(String replacementString)
	{
		this.matchReplacement = replacementString;
	}

	/**
	 * @return the extendsField
	 */
	public String getExtendsField()
	{
		return extendsField;
	}

	/**
	 * @param extendsField the extendsField to set
	 */
	public void setExtendsField(String extendsField)
	{
		this.extendsField = extendsField;
	}

	/**
	 * @return the mmdRepository
	 */
	public MetaMetadataRepository getMmdRepository()
	{
		return mmdRepository;
	}

	/**
	 * @param mmdRepository the mmdRepository to set
	 */
	public void setMmdRepository(MetaMetadataRepository mmdRepository)
	{
		this.mmdRepository = mmdRepository;
	}

	/**
	 * @return the contextNode
	 */
	public final String getContextNode()
	{
		return contextNode;
	}
	
	
	public String parentString()
	{
		String result	= "";
		
		ElementState parent = parent();
		if (parent instanceof MetaMetadataField)
		{
			MetaMetadataField pf	= (MetaMetadataField) parent;
			result = "<" + pf.name + ">";
		}
		return result;
	}
	String toString;
	
	public String toString()
	{
		String result	= toString;
		if (result == null)
		{
			result 		= getClassName() + parentString() + "<" + name + ">";
			toString	= result;
		}
		return result;
	}
	
	/*public String getChildTag()
	{
		if(childTag!=null && noWrap)
		{
			return childTag;
		}
		else if (tag != null && collection == null)
		{
			// TODO implement other cases
			return tag;
		}
		else
			return name;	
	}	*/
	
	protected void bindChildren(MetaMetadata childMM)
	{
		if (childMM != null)
		{
			kids	= childMM.kids;
		}

	}

	/**
	 * @return the tag
	 */
	public String resolveTag()
	{
		return (tag != null) ? tag : name;
		// return (isNoWrap()) ? ((childTag != null) ? childTag : childType) : (tag != null) ? tag : name;
	}
	
	/**
	 * If a tag was declared, form an ecologylab.xml @xml_tag declaration with it.
	 * 
	 * @return	The @xml_tag declaration string, or the empty string.
	 */
	public String getTagDecl()
	{
		boolean hasTag = tag != null && tag.length() > 0;
		return hasTag ? "@xml_tag(\""+tag+"\")" : "";
	}
	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag)
	{
		this.tag = tag;
	}

	@Override
  protected void postTranslationProcessingHook()
  {

  }

	/**
	 * @return the metadataFieldDescriptor
	 */
	public MetadataFieldDescriptor getMetadataFieldDescriptor()
	{
		return metadataFieldDescriptor;
	}

	/**
	 * @param metadataFieldDescriptor the metadataFieldDescriptor to set
	 */
	void setMetadataFieldDescriptor(MetadataFieldDescriptor metadataFieldDescriptor)
	{
		this.metadataFieldDescriptor = metadataFieldDescriptor;
	}
	
	
	/**
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	protected void setName(String name)
	{
		this.name = name;
	}
	
	public String getTagForTranslationScope()
	{
		return tag != null ? tag : name;
	}
	
	public File getFile()
	{
		if (file != null)
			return file;
		MetaMetadataField parent	= (MetaMetadataField) parent();
		return (parent != null) ? parent.getFile() : null;
	}

	abstract protected void doAppending(Appendable appendable, int pass) throws IOException;
	
	protected static class LayerComparator implements Comparator<MetaMetadataField>
	{

		@Override
		public int compare(MetaMetadataField o1, MetaMetadataField o2)
		{
			// return negation for descending ordering in sort
			return -Float.compare(o1.layer, o2.layer);
		}
		
	}
	
	public String getDisplayedLabel()
	{
		return (label != null) ? label : name;
	}
}
