package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.generic.HashMapArrayList;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataClassDescriptor;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;
import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.semantics.tools.MetadataCompilerUtils;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_inherit;
import ecologylab.serialization.types.element.Mappable;
import ecologylab.serialization.types.scalar.ScalarType;
import ecologylab.textformat.NamedStyle;

/**
 * 
 * @author damaraju
 * 
 */
@simpl_inherit
public abstract class MetaMetadataField extends ElementState implements Mappable<String>,
		PackageSpecifier, Iterable<MetaMetadataField>
{

	static class LayerComparator implements Comparator<MetaMetadataField>
	{

		@Override
		public int compare(MetaMetadataField o1, MetaMetadataField o2)
		{
			// return negation for descending ordering in sort
			return -Float.compare(o1.layer, o2.layer);
		}

	}

	static LayerComparator																LAYER_COMPARATOR						= new LayerComparator();

	static ArrayList<MetaMetadataField>	EMPTY_COLLECTION	= new ArrayList<MetaMetadataField>(0);

	static Iterator<MetaMetadataField>	EMPTY_ITERATOR		= EMPTY_COLLECTION.iterator();

	MetadataFieldDescriptor																metadataFieldDescriptor;

	@simpl_scalar
	protected String																			name;

	@simpl_scalar
	protected String																			comment;

	@simpl_scalar
	protected String																			tag;

	@simpl_scalar
	protected String																			xpath;

	/**
	 * Context node for xpath based extarction rules for this field. Default value is document root.
	 */
	@simpl_scalar
	protected String																			contextNode;

	@xml_tag("extends")
	@simpl_scalar
	protected String																			extendsAttribute;
	
	@simpl_scalar
	protected String																			fieldParserKey;

	@simpl_map
	@simpl_classes(
	{ MetaMetadataField.class, MetaMetadataScalarField.class, MetaMetadataCompositeField.class,
			MetaMetadataCollectionField.class, })
	@simpl_nowrap
	protected HashMapArrayList<String, MetaMetadataField>	kids;

	// ///////////////////////////////// visualization fields /////////////////////////////////

	/**
	 * true if this field should not be displayed in interactive in-context metadata
	 */
	@simpl_scalar
	protected boolean																			hide;

	/**
	 * If true the field is shown even if its null or empty.
	 */
	@simpl_scalar
	protected boolean																			alwaysShow;

	@simpl_scalar
	protected String																			style;

	/**
	 * Specifies the order in which a field is displayed in relation to other fields.
	 */
	@simpl_scalar
	protected float																				layer;

	/**
	 * Another field name that this field navigates to (e.g. from a label in in-context metadata)
	 */
	@simpl_scalar
	protected String																			navigatesTo;

	/**
	 * This MetaMetadataField shadows another field, so it is to be displayed instead of the other.It
	 * is kind of over-riding a field.
	 */
	@simpl_scalar
	protected String																			shadows;

	/**
	 * The label to be used when visualizing this field. Name is used by default. This overrides name.
	 */
	@simpl_scalar
	protected String																			label;

	// ///////////////////////////////// switches /////////////////////////////////

	@simpl_scalar
	protected boolean																			isFacet;

	@simpl_scalar
	protected boolean																			ignoreInTermVector;

	// ///////////////////////////////// members /////////////////////////////////

	HashSet<String>																				nonDisplayedFieldNames;

	File																									file;

	private boolean																				fieldsSortedForDisplay			= false;

	private String																				displayedLabel							= null;

	/************* These 2 variables are needed fo inheritence implementation *********************/
	/**
	 * Holds the super class of the meta-metadata to which this field belongs
	 */
	private String																				extendsField;

	/**
	 * The Meta-Metadata repository object.
	 */
	private MetaMetadataRepository												repository;

	protected boolean																			inheritMetaMetadataFinished	= false;

	/**************************************************************************************/

	/**
	 * Class of the Metadata object that corresponds to this. Non-null for nested and collection
	 * fields. Null for scalar fields.
	 */
	private Class<? extends Metadata>	metadataClass;

	/**
	 * Class descriptor for the Metadata object that corresponds to this. Non-null for nested and
	 * collection fields. Null for scalar fields.
	 */
	protected MetadataClassDescriptor	metadataClassDescriptor;

	String	toString;

	public MetaMetadataField()
	{

	}

	public MetaMetadataField(String name, HashMapArrayList<String, MetaMetadataField> children)
	{
		this.name = name;
		this.kids = children;
	}

	protected MetaMetadataField(MetaMetadataField copy, String name)
	{
		this();
		this.name = name;
		this.tag = copy.tag;
		this.extendsField = copy.extendsField;
		this.kids = copy.kids;

		// TODO -- do we need to propagate more fields here?

		// this.childType = copy.childType;
		// this.childTag = copy.childTag;
		// this.noWrap = copy.noWrap;
	}

	public HashMapArrayList<String, MetaMetadataField> getChildMetaMetadata()
	{
		return kids;
	}

	public String getComment()
	{
		return comment;
	}

	public final String getContextNode()
	{
		return contextNode;
	}

	public String getDisplayedLabel()
	{
		String result = displayedLabel;
		if (result == null)
		{
			if (label != null)
				result = label;
			else
				result = name.replace("_", " ");

			displayedLabel = result;
		}
		return result;
	}

	/**
	 * @return the extendsAttribute
	 */
	public String getExtendsAttribute()
	{
		return extendsAttribute;
	}

	/**
	 * @return the extendsField
	 */
	public String getExtendsField()
	{
		return extendsField;
	}
	
	public String getFieldParserKey()
	{
		return fieldParserKey;
	}

	public String getFieldName()
	{
		return XMLTools.fieldNameFromElementName(getName());
	}

	public File getFile()
	{
		if (file != null)
			return file;
		MetaMetadataField parent = (MetaMetadataField) parent();
		return (parent != null) ? parent.getFile() : null;
	}

	public Class<? extends Metadata> getMetadataClass()
	{
		return metadataClass;
	}

	/**
	 * Lookup the Metadata class object that corresponds to tag_name, type, or extends attribute
	 * depending on which exist.
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
			String tagForTranslationScope = getTagForTranslationScope();
			result = (Class<? extends Metadata>) ts.getClassByTag(tagForTranslationScope);
			if (result == null)
			{
				if (this instanceof MetaMetadataCompositeField)
					result = (Class<? extends Metadata>) ts.getClassByTag(((MetaMetadataCompositeField) this)
							.getTypeOrName());
				if (result == null)
				{
					// there is no class for this tag we can use class of meta-metadata it extends
					result = (Class<? extends Metadata>) ts.getClassByTag(extendsAttribute);
				}
			}
			if (result != null)
				this.metadataClass = result;
			else
				ts.error("Can't resolve: " + this + " using " + tagForTranslationScope);
		}
		return result;
	}

	/**
	 * @return the metadataClassDescriptor
	 */
	public MetadataClassDescriptor getMetadataClassDescriptor()
	{
		return metadataClassDescriptor;
	}

	/**
	 * @return the metadataFieldDescriptor
	 */
	public MetadataFieldDescriptor getMetadataFieldDescriptor()
	{
		return metadataFieldDescriptor;
	}

	public String getName()
	{
		return name;
	}

	public String getNavigatesTo()
	{
		return navigatesTo;
	}

	public MetaMetadataRepository getRepository()
	{
		if (repository == null)
			repository = findRepository();
		return repository;
	}

	/**
	 * If a tag was declared, form an ecologylab.serialization @xml_tag declaration with it.
	 * 
	 * @return The @xml_tag declaration string, or the empty string.
	 */
	public String getTagDecl()
	{
		boolean hasTag = tag != null && tag.length() > 0;
		return hasTag ? "@xml_tag(\"" + tag + "\")" : "";
	}

	public String getTagForTranslationScope()
	{
		return tag != null ? tag : name;
	}

	public String getType()
	{
		return null;
	}

	public String getXpath()
	{
		return xpath;
	}

	/**
	 * Connect the appropriate MetadataClassDescriptor with this, and likewise, recursively perform
	 * this binding operation for all the children of this.
	 * 
	 * This method will remove this metametadata field from it's parent when no appropriate metadata subclass 
	 * was found. 
	 * 
	 * @param metadataTScope
	 * @return
	 */
	boolean getClassAndBindDescriptors(TranslationScope metadataTScope)
	{
		Class<? extends Metadata> metadataClass = getMetadataClass(metadataTScope);
		if (metadataClass == null)
		{
			ElementState parent = parent();
			if (parent instanceof MetaMetadataField)
				((MetaMetadataField) parent).kids.remove(this.getName()); 
			else if (parent instanceof MetaMetadataRepository)
			{
				// TODO remove from the repository level
			}
			return false;
		}
		//
		bindClassDescriptor(metadataClass, metadataTScope);
		return true;
	}

	public boolean isAlwaysShow()
	{
		return alwaysShow;
	}

	public boolean isChildFieldDisplayed(String childName)
	{
		return nonDisplayedFieldNames == null ? true : !nonDisplayedFieldNames.contains(childName);
	}

	public boolean isHide()
	{
		return hide;
	}

	public boolean isIgnoreInTermVector()
	{
		return ignoreInTermVector;
	}

	public int numNonDisplayedFields()
	{
		return nonDisplayedFieldNames == null ? 0 : nonDisplayedFieldNames.size();
	}

	public String parentString()
	{
		String result = "";
	
		ElementState parent = parent();
		if (parent instanceof MetaMetadataField)
		{
			MetaMetadataField pf = (MetaMetadataField) parent;
			result = "<" + pf.name + ">";
		}
		return result;
	}

	public String shadows()
	{
		return shadows;
	}

	public int size()
	{
		return kids == null ? 0 : kids.size();
	}

	public void sortForDisplay()
	{
		if (!fieldsSortedForDisplay)
		{
	
			HashMapArrayList<String, MetaMetadataField> childMetaMetadata = getChildMetaMetadata();
			if (childMetaMetadata != null)
				Collections.sort((ArrayList<MetaMetadataField>) childMetaMetadata.values(),
						LAYER_COMPARATOR);
			fieldsSortedForDisplay = true;
		}
	}

	public boolean hasChildren()
	{
		return kids != null && kids.size() > 0;
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

	/**
	 * @param childMetaMetadata
	 *          the childMetaMetadata to set
	 */
	public void setChildMetaMetadata(HashMapArrayList<String, MetaMetadataField> childMetaMetadata)
	{
		this.kids = childMetaMetadata;
	}

	/**
	 * @param extendsField
	 *          the extendsField to set
	 */
	public void setExtendsField(String extendsField)
	{
		this.extendsField = extendsField;
	}

	protected void setName(String name)
	{
		this.name = name;
	}

	public void setRepository(MetaMetadataRepository repository)
	{
		this.repository = repository;
	}

	/**
	 * @param tag
	 *          the tag to set
	 */
	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public Iterator<MetaMetadataField> iterator()
	{
		HashMapArrayList<String, MetaMetadataField> childMetaMetadata = getChildMetaMetadata();
		return (childMetaMetadata != null) ? childMetaMetadata.iterator() : EMPTY_ITERATOR;
	}

	public String key()
	{
		return name;
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

	public String toString()
	{
		String result = toString;
		if (result == null)
		{
			result = getClassName() + parentString() + "<" + name + ">";
			toString = result;
		}
		return result;
	}

	public HashMapArrayList<String, MetaMetadataField> initializeChildMetaMetadata()
	{
		this.kids = new HashMapArrayList<String, MetaMetadataField>();
		return this.kids;
	}

	/**
	 * check for errors.
	 * 
	 * @return true if nothing goes wrong; false if there is something wrong.
	 */
	abstract protected boolean checkForErrors();

	/**
	 * do we need to generate a new class definition (e.g. java source file) for this field? 
	 * 
	 * @return
	 */
	abstract public boolean isNewClass();

	/**
	 * is this a new declaration (instead of an inherited one), so that we should generate member
	 * declaration & getter/setters for this field?
	 * 
	 * @return
	 */
	abstract protected boolean isNewDeclaration();

	abstract protected void doAppending(Appendable appendable, int pass) throws IOException;

	public MetaMetadataField lookupChild(MetadataFieldDescriptor metadataFieldDescriptor)
	{
		return lookupChild(XMLTools.getXmlTagName(metadataFieldDescriptor.getFieldName(), null));
	}

	public MetaMetadataField lookupChild(String name)
	{
		return kids.get(name);
	}

	public NamedStyle lookupStyle()
	{
		return (style != null) ? getRepository().lookupStyle(style) : getRepository().getDefaultStyle();
	}

	/**
	 * @return the tag
	 */
	public String resolveTag()
	{
		return (tag != null) ? tag : name;
		// return (isNoWrap()) ? ((childTag != null) ? childTag : childType) : (tag != null) ? tag :
		// name;
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
		// this is for the case when meta_metadata has no meta_metadata fields of its own. It just
		// inherits from super class.
		HashMapArrayList<String, MetaMetadataField> childMetaMetadata = getChildMetaMetadata();
		if (childMetaMetadata == null)
		{
			childMetaMetadata = initializeChildMetaMetadata();
		}
	
		// *do not* override fields in here with fields from super classes.
	
		MetaMetadataField fieldToInheritTo = childMetaMetadata.get(fieldName);
		if (fieldToInheritTo == null)
		{
			childMetaMetadata.put(fieldName, fieldToInheritFrom);
			fieldToInheritTo = fieldToInheritFrom;
		}
		else
		{
			fieldToInheritTo.inheritNonDefaultAttributes(fieldToInheritFrom);
		}
	
		HashMapArrayList<String, MetaMetadataField> inheritedChildMetaMetadata = fieldToInheritFrom
				.getChildMetaMetadata();
		if (inheritedChildMetaMetadata != null)
		{
			for (MetaMetadataField grandChildMetaMetadataField : inheritedChildMetaMetadata)
			{
				fieldToInheritTo.inheritForField(grandChildMetaMetadataField);
			}
		}
	
	}

	HashSet<String> nonDisplayedFieldNames()
	{
		HashSet<String> result = this.nonDisplayedFieldNames;
		if (result == null)
		{
			result = new HashSet<String>();
			this.nonDisplayedFieldNames = result;
		}
		return result;
	}

	/**
	 * @param metadataFieldDescriptor
	 *          the metadataFieldDescriptor to set
	 */
	void setMetadataFieldDescriptor(MetadataFieldDescriptor metadataFieldDescriptor)
	{
		this.metadataFieldDescriptor = metadataFieldDescriptor;
	}

	protected String generateNewClassName()
	{
		String javaClassName = null;
	
		if (this instanceof MetaMetadataCollectionField)
		{
			javaClassName = ((MetaMetadataCollectionField) this).childType;
		}
		else
		{
			javaClassName = ((MetaMetadataCompositeField) this).getTypeOrName();
		}
	
		return javaClassName;
	}
	
	public void compileToMetadataClass(String packageName, Appendable appendable, int pass,
			boolean appendedToTranslastionScope) throws SIMPLTranslationException, IOException
	{
		
		// TODO import paths
	
		// TODO track generated classes for TranslationScope declaration
		
		if (!checkForErrors())
			return;
	
		doAppending(appendable, pass);
	
		// new java class has to be written
		if (isNewClass())
		{
			// FIXME -- call the regular routine for generating a class declaration!!!!!!!!!!!!!! code
			// should not be duplicated
			// getting the generation path for the java class.
			String generationPath = MetadataCompilerUtils.getGenerationPath(packageName);
	
			// the name of the java class.
			String metaMetadataName = generateNewClassName();
			
			MetaMetadataField existent = getRepository().getByTagName(metaMetadataName);
			if (existent != null)
			{
				warning("overriding existent meta-metadata: " + existent.packageName() + "." + metaMetadataName);
			}
	
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
			String javaClassName = XMLTools.classNameFromElementName(metaMetadataName);
			// file writer.
			File directoryPath = PropertiesAndDirectories.createDirsAsNeeded(new File(generationPath));
	
			File f = new File(directoryPath, javaClassName + ".java");
			// write to console
			if (pass == MetadataCompilerUtils.GENERATE_FIELDS_PASS)
				System.out.println("\n\t\t -> " + f);
	
			FileWriter fileWriter = new FileWriter(f);
			PrintWriter p = new PrintWriter(fileWriter);
	
			// writing the package declaration
			p.println(MetadataCompilerUtils.PACKAGE + " " + packageName + ";");
	
			// writing the imports
			// p.println(MetadataCompiler.getImportStatement());
			MetadataCompiler.printImports(p);
	
			// write xml_inherit
			p.println("@simpl_inherit");
			// p.println("@xml_tag(\""+collectionChildType+"\")");
			p.println(getTagDecl());
	
			// start of class definition
			p.println("public class " + javaClassName + " extends Metadata" + implementDecl + "{\n");
	
			// loop to write the class definition.
	
			HashMapArrayList<String, MetaMetadataField> childMetaMetadata = getChildMetaMetadata();
			for (int i = 0; i < childMetaMetadata.size(); i++)
			{
				// translate the each meta-metadata field into class.
				MetaMetadataField cField = childMetaMetadata.get(i);
				cField.setExtendsField(extendsField);
				cField.setRepository(repository);
				cField.compileToMetadataClass(packageName, p, MetadataCompilerUtils.GENERATE_FIELDS_PASS,
						false);
			}
	
			// write the constructors
			MetadataCompilerUtils.appendBlankConstructor(p, javaClassName);
			MetadataCompilerUtils.appendConstructor(p, javaClassName);
	
			for (int i = 0; i < childMetaMetadata.size(); i++)
			{
				// translate the each meta-metadata field into class.
				MetaMetadataField cField = childMetaMetadata.get(i);
				cField.setExtendsField(extendsField);
				cField.setRepository(repository);
				cField.compileToMetadataClass(packageName, p, MetadataCompilerUtils.GENERATE_METHODS_PASS,
						true);
			}
	
			// if this is a Map we have to implement the key() method.
			/*
			 * if(isMap) { appendKeyMethod(p); }
			 */
			// ending the class.
			p.println("}");
			p.flush();
	
			if (!appendedToTranslastionScope)
			{
				// append this class to generated translation scope
				MetadataCompilerUtils.appendToTranslationScope(javaClassName + ".class,\n");
			}
		}
	}

	protected void inheritNonDefaultAttributes(MetaMetadataField inheritFrom)
	{
		ClassDescriptor<?, ? extends FieldDescriptor> classDescriptor = classDescriptor();
	
		for (FieldDescriptor fieldDescriptor : classDescriptor)
		{
			ScalarType scalarType = fieldDescriptor.getScalarType();
			try
			{
				if (scalarType != null && scalarType.isDefaultValue(fieldDescriptor.getField(), this)
						&& !scalarType.isDefaultValue(fieldDescriptor.getField(), inheritFrom))
				{
					Object value = fieldDescriptor.getField().get(inheritFrom);
					fieldDescriptor.setField(this, value);
					debug("inherit\t" + this.getName() + "." + fieldDescriptor.getFieldName() + "\t= "
							+ value);
				}
			}
			catch (IllegalArgumentException e)
			{
				// TODO Auto-generated catch block
				debug(inheritFrom.getName() + " doesn't have field " + fieldDescriptor.getFieldName()
						+ ", ignore it.");
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Hook overrided by MetaMetadata class
	 * 
	 * @param inheritedMetaMetadata
	 */
	protected void inheritNonFieldComponentsFromMM(MetaMetadata inheritedMetaMetadata)
	{
		// MetaMetadataFields don't have semantic actions.
	}

	/**
	 * Append an @simpl_composite declaration to appendable, using the name of this to directly form
	 * both the class name and the field name.
	 * 
	 * @param appendable
	 * @throws IOException
	 */
	private void appendNested(Appendable appendable) throws IOException
	{
		String elementName = getName();
		appendNested(appendable, "", XMLTools.classNameFromElementName(elementName),
				XMLTools.fieldNameFromElementName(elementName));
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
		appendMetalanguageDecl(appendable, getTagDecl() + " @simpl_composite", classNamePrefix,
				className, fieldName);
	}

	private MetaMetadataRepository findRepository()
	{
		ElementState parent = parent();
		while (parent != null && !(parent instanceof MetaMetadataRepository))
			parent = parent.parent();
		if (parent == null)
		{
			error("can't find repository for " + this);
			return null;
		}
		return (MetaMetadataRepository) parent;
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
	 * 
	 * public void hwSetQueryMetadata(MetadataString query) { if (this.query != null &&
	 * this.query.getValue() != null && hasTermVector()) termVector().remove(this.query.termVector());
	 * this.query = query; rebuildCompositeTermVector(); }
	 * 
	 * @param appendable
	 * @param fieldName
	 * @param string
	 * @throws IOException
	 */
	protected void appendDirectHWSetMethod(Appendable appendable, String fieldName,
			String fieldTypeName) throws IOException
	{
		String comment = "Heavy Weight Direct setter method for " + fieldName;

		// write the java doc comment
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void hwSet" + XMLTools.javaNameFromElementName(fieldName, true)
				+ "Metadata(" + fieldTypeName + " " + fieldName + ")\n{");

		// second line
		appendable.append("\t if(this." + fieldName + "!=null && this." + fieldName
				+ ".getValue()!=null && hasTermVector())\n");

		// third line
		appendable.append("\t\t termVector().remove(this." + fieldName + ".termVector());\n");

		// fourth line
		appendable.append("\t this." + fieldName + " = " + fieldName + ";\n");

		// last line
		appendable.append("\trebuildCompositeTermVector();\n}");
	}

	/**
	 * public void setQueryMetadata(MetadataString query) { this.query = query; }
	 * 
	 * @param appendable
	 * @param fieldName
	 * @param fieldTypeName
	 * @throws IOException
	 */
	protected void appendDirectSetMethod(Appendable appendable, String fieldName, String fieldTypeName)
			throws IOException
	{
		String comment = " Sets the " + fieldName + " directly";

		// write the java doc comment
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line
		appendable.append("public void set" + XMLTools.javaNameFromElementName(fieldName, true)
				+ "Metadata(" + fieldTypeName + " " + fieldName + ")\n{");

		// second line
		appendable.append("\tthis." + fieldName + " = " + fieldName + ";\n}");

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

	protected void appendImport(Appendable appendable, String importDecl) throws IOException
	{
		appendable.append("import ").append(importDecl).append(';').append('\n');
	}

	/**
	 * MetadataString title() { MetadataString result = this.title; if(result == null) { result = new
	 * MetadataString(); this.title = result; } return result; }
	 * 
	 * @throws IOException
	 */
	protected void appendLazyEvaluationMethod(Appendable appendable, String fieldName,
			String fieldType) throws IOException
	{
		String comment = "Lazy Evaluation for " + fieldName;

		String returnType = fieldType;

		// write comment for this method
		MetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// first line . Start of method name
		appendable.append("public ").append(returnType).append("\t").append(fieldName)
				.append("()\n{\n");

		// second line. Declaration of result variable.
		appendable.append(returnType).append("\t").append("result\t=this.").append(fieldName)
				.append(";\n");

		// third line . Start of if statement
		appendable.append("if(result == null)\n{\n");

		// fourth line. creation of new result object.
		appendable.append("result = new ").append(returnType).append("();\n");

		// fifth line . end of if statement
		appendable.append("this.").append(fieldName).append("\t=\t result;\n}\n");

		// sixth line. return statement and end of method.
		appendable.append("return result;\n}\n");

	}

	protected void appendMetalanguageDecl(Appendable appendable, String metalanguage,
			String classNamePrefix, String className, String fieldName) throws IOException
	{
		appendMetalanguageDecl(appendable, metalanguage, classNamePrefix, className, "", fieldName);
	}

	protected void appendMetalanguageDecl(Appendable appendable, String metalanguage,
			String classNamePrefix, String className, String classNameSuffix, String fieldName)
			throws IOException
	{
		appendable.append('\t').append(metalanguage).append(' ').append(classNamePrefix)
				.append(className).append(classNameSuffix).append('\t');
		appendable.append(fieldName).append(';').append('\n');
	}

	/**
	 * Compute the map of FieldDescriptors for this class, with the field names as key, but with the
	 * mixins field removed.
	 * 
	 * @param metadataTScope
	 *          TODO
	 * @param metadataClassDescriptor
	 *          TODO
	 * 
	 * @return A map of FieldDescriptors, with the field names as key, but with the mixins field
	 *         removed.
	 */
	protected final void bindMetadataFieldDescriptors(TranslationScope metadataTScope,
			MetadataClassDescriptor metadataClassDescriptor)
	{
		for (MetaMetadataField thatChild : kids)
		{
			thatChild.bindMetadataFieldDescriptor(metadataTScope, metadataClassDescriptor);

			if (thatChild instanceof MetaMetadataScalarField)
			{
				MetaMetadataScalarField scalar = (MetaMetadataScalarField) thatChild;
				if (scalar.getRegexPattern() != null)
				{
					MetadataFieldDescriptor fd = scalar.getMetadataFieldDescriptor();
					fd.setRegexFilter(Pattern.compile(scalar.getRegexPattern()), scalar.getRegexReplacement());
				}
			}

			HashSet<String> nonDisplayedFieldNames = nonDisplayedFieldNames();
			if (thatChild.hide)
				nonDisplayedFieldNames.add(thatChild.name);
			if (thatChild.shadows != null)
				nonDisplayedFieldNames.add(thatChild.shadows);

			// recursive descent
			if (thatChild.hasChildren())
				thatChild.getClassAndBindDescriptors(metadataTScope);
		}
		// for (FieldDescriptor fieldDescriptor : allFieldDescriptorsByFieldName.values())
		// {
		// String tagName = fieldDescriptor.getTagName();
		// result.put(tagName, (MetadataFieldDescriptor) fieldDescriptor);
		// }
	}

	/**
	 * Obtain a map of FieldDescriptors for this class, with the field names as key, but with the
	 * mixins field removed. Use lazy evaluation, caching the result by class name.
	 * 
	 * @param metadataTScope
	 *          TODO
	 * 
	 * @return A map of FieldDescriptors, with the field names as key, but with the mixins field
	 *         removed.
	 */
	final void bindClassDescriptor(Class<? extends Metadata> metadataClass,
			TranslationScope metadataTScope)
	{
		MetadataClassDescriptor metadataClassDescriptor = this.metadataClassDescriptor;
		if (metadataClassDescriptor == null)
		{
			synchronized (this)
			{
				metadataClassDescriptor = this.metadataClassDescriptor;
				if (metadataClassDescriptor == null)
				{
					metadataClassDescriptor = (MetadataClassDescriptor) ClassDescriptor
							.getClassDescriptor(metadataClass);
					bindMetadataFieldDescriptors(metadataTScope, metadataClassDescriptor);
					this.metadataClassDescriptor = metadataClassDescriptor;
				}
			}
		}
	}

	void bindMetadataFieldDescriptor(TranslationScope metadataTScope,
			MetadataClassDescriptor metadataClassDescriptor)
	{
		String tagName = this.resolveTag(); // TODO -- is this the correct tag?
		MetadataFieldDescriptor metadataFieldDescriptor = (MetadataFieldDescriptor) metadataClassDescriptor
				.getFieldDescriptorByTag(tagName, metadataTScope);
		if (metadataFieldDescriptor != null)
		{
			// if we don't have a field, then this is a wrapped collection, so we need to get the wrapped
			// field descriptor
			if (metadataFieldDescriptor.getField() == null)
				metadataFieldDescriptor = (MetadataFieldDescriptor) metadataFieldDescriptor.getWrappedFD();

			this.setMetadataFieldDescriptor(metadataFieldDescriptor);
		}
		else
		{
			warning("Ignoring <" + tagName
					+ "> because no corresponding MetadataFieldDescriptor can be found.");
		}

	}

	@Override
	protected void deserializationPostHook()
	{
	
	}
	
	/**
	 * util function making sure that an object is null.
	 * @param obj
	 * @param errorMsgFmt
	 * @param vars
	 * @return true if obj is null, otherwise false.
	 */
	protected boolean assertNull(Object obj, String errorMsgFmt, Object... vars)
	{
		if (obj != null)
		{
			String err = String.format(errorMsgFmt, vars);
			error(err);
			return false;
		}
		return true;
	}
	
	/**
	 * util function making sure that an object is not null.
	 * @param obj
	 * @param errorMsgFmt
	 * @param vars
	 * @return true if obj is not null, otherwise false.
	 */
	protected boolean assertNotNull(Object obj, String errorMsgFmt, Object... vars)
	{
		if (obj == null)
		{
			String err = String.format(errorMsgFmt, vars);
			error(err);
			return false;
		}
		return true;
	}
	
	/**
	 * util function making sure that two objects are equal (by calling equals()).
	 * @param obj1
	 * @param obj2
	 * @param errorMsgFmt
	 * @param vars
	 * @return true if they are equal, otherwise false.
	 */
	protected boolean assertEquals(Object obj1, Object obj2, String errorMsgFmt, Object... vars)
	{
		if (!obj1.equals(obj2))
		{
			String err = String.format(errorMsgFmt, vars);
			error(err);
			return false;
		}
		return true;
	}
	
	/**
	 * @return the type name of this field (or meta-metadata). for meta-metadata, it returns its name
	 * cause its name is actually a type name. for definitive fields, it tries type= / child_type= and
	 * name in order; for decorative fields, it tries type= / child_type= and inherited type name in
	 * order.
	 */
	abstract protected String getTypeName();
	
	/**
	 * @return the super type name of this field (or meta-metadata). for meta-metadata, it returns
	 * type= if specified (indicating this is a <b>decorative</b> meta-metadata), otherwise extends=
	 * or "metadata". for fields, it returns extends= or "metadata".
	 */
	abstract protected String getSuperTypeName();
	
	/**
	 * @return the field object from which this field inherits.
	 */
	protected MetaMetadataField getInheritedField()
	{
		MetaMetadataNestedField parent = (MetaMetadataNestedField) parent();
		MetaMetadataNestedField parentInherited = (MetaMetadataNestedField) parent.getInheritedField();
		return parentInherited == null ? null : parentInherited.lookupChild(getName());
	}

}
