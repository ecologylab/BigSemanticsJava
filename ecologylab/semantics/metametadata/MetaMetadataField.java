package ecologylab.semantics.metametadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import ecologylab.semantics.metadata.Metadata.mm_dont_inherit;
import ecologylab.semantics.tools.MetaMetadataCompiler;
import ecologylab.semantics.tools.MetaMetadataCompilerUtils;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldTypes;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_descriptor_classes;
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
@simpl_descriptor_classes({ MetaMetadataClassDescriptor.class, MetaMetadataFieldDescriptor.class })
public abstract class MetaMetadataField extends ElementState implements Mappable<String>, Iterable<MetaMetadataField>, MMDConstants, Cloneable
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

	static LayerComparator																LAYER_COMPARATOR				= new LayerComparator();

	static ArrayList<MetaMetadataField>										EMPTY_COLLECTION				= new ArrayList<MetaMetadataField>(0);

	static Iterator<MetaMetadataField>										EMPTY_ITERATOR					= EMPTY_COLLECTION.iterator();
	
	protected static HashSet<MetaMetadataField>						visitedMetaMetadata;

	MetadataFieldDescriptor																metadataFieldDescriptor;

	@simpl_scalar
	protected String																			name;

	@simpl_scalar
	@mm_dont_inherit
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

	@simpl_scalar
	protected String																			fieldParserKey;

	@simpl_scalar
	protected String																			schemaOrgItemprop;

	// initializing kids here seems a waste of space, but I would argue for this because this field
	// will get created during the inheritance process anyway. -- yin
	@simpl_map
	@simpl_classes({ MetaMetadataField.class, MetaMetadataScalarField.class, MetaMetadataCompositeField.class, MetaMetadataCollectionField.class, })
	@simpl_nowrap
	protected HashMapArrayList<String, MetaMetadataField>	kids = new HashMapArrayList<String, MetaMetadataField>();

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

	/**
	 * The name of natural id if this field is used as one.
	 */
	@simpl_scalar
	protected String																			asNaturalId;

	/**
	 * The format of this field. Used for normalization. Currently, only used with natural ids.
	 */
	@simpl_scalar
	protected String																			format;

	/**
	 * Indicate if this field is required for the upper level structure.
	 */
	@simpl_scalar
	protected boolean																			required								= false;

	@simpl_scalar
	protected boolean																			dontSerialize						= false;

	// ///////////////////////////////// switches /////////////////////////////////

	@simpl_scalar
	protected boolean																			isFacet;

	@simpl_scalar
	protected boolean																			ignoreInTermVector;

	@simpl_scalar
	protected boolean																			ignoreCompletely;
	
	// ///////////////////////////////// members /////////////////////////////////

	HashSet<String>																				nonDisplayedFieldNames;

	File																									file;

	private boolean																				fieldsSortedForDisplay	= false;

	private String																				displayedLabel					= null;

	/**
	 * The Meta-Metadata repository object.
	 */
	private MetaMetadataRepository												repository;

	protected boolean																			inheritFinished					= false;

	/**
	 * inheritInProcess prevents infinite loops, e.g. when A.b refers B while B.a refers A, then when
	 * you initialize A.b you will have to initialize A.b.a and you will have to initialize A.b.a.b
	 * ...
	 */
	protected boolean																			inheritInProcess				= false;

	private boolean																				fieldInherited					= false;

	private boolean																				bindDescriptorsFinished	= false;

	/**
	 * Class of the Metadata object that corresponds to this. Non-null for nested and collection
	 * fields. Null for scalar fields.
	 */
	private Class<? extends Metadata>											metadataClass;

	/**
	 * Class descriptor for the Metadata object that corresponds to this. Non-null for nested and
	 * collection fields. Null for scalar fields.
	 */
	protected MetadataClassDescriptor											metadataClassDescriptor;

	String																								toString;

	/**
	 * for caching getInheritedField().
	 */
	private MetaMetadataField															inheritedField					= null;

	/**
	 * in which meta-metadata this field is declared.
	 */
	private MetaMetadata																	declaringMmd						= null;
	
	private ArrayList<String>															otherTags								= null;

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
		this.kids = copy.kids;

		// TODO -- do we need to propagate more fields here?

		// this.childType = copy.childType;
		// this.childTag = copy.childTag;
		// this.noWrap = copy.noWrap;
	}
	
	abstract protected Object clone() throws CloneNotSupportedException;
	
	protected void copyClonedFieldsFrom(MetaMetadataField other)
	{
		//this.metadataFieldDescriptor = other.metadataFieldDescriptor;
		this.displayedLabel = other.displayedLabel;
		this.repository = other.repository;
		//this.metadataClass = other.metadataClass;
		//this.metadataClassDescriptor = other.metadataClassDescriptor;
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

	public String getFieldParserKey()
	{
		return fieldParserKey;
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
				{
					MetaMetadataCompositeField mmcf = (MetaMetadataCompositeField) this;
					
					// if type= defined, use it
					String type = mmcf.getType();
					if (type != null)
					{
						result = (Class<? extends Metadata>) ts.getClassByTag(type);
						if (result == null)
						{
							// the type name doesn't work, but we can try super mmd class
							MetaMetadata superMmd = getRepository().getByTagName(type);
							if (superMmd != null)
								result = superMmd.getMetadataClass(ts);
						}
					}
					
					if (result == null)
					{
						// then try name
						String name = mmcf.getName();
						result = (Class<? extends Metadata>) ts.getClassByTag(name);
					}
					
					if (result == null && this instanceof MetaMetadataCompositeField)
					{
						// if type and name don't work, try extends=
						// there is no class for this tag we can use class of meta-metadata it extends
						result = (Class<? extends Metadata>) ts.getClassByTag(((MetaMetadataCompositeField)this).getExtendsAttribute());
					}
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
	
	protected void setMetadataClassDescriptor(MetadataClassDescriptor cd)
	{
		this.metadataClassDescriptor = cd;
	}

	/**
	 * @return the metadataFieldDescriptor
	 */
	public MetadataFieldDescriptor getMetadataFieldDescriptor()
	{
		return metadataFieldDescriptor;
	}
	
	public String getStyle()
	{
		return style;
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

	@Deprecated
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
		return hide || isIgnoreCompletely();
	}

	public boolean isIgnoreInTermVector()
	{
		return ignoreInTermVector || isIgnoreCompletely();
	}
	
	public boolean isIgnoreCompletely()
	{
		return ignoreCompletely;
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

	/**
	 * @param childMetaMetadata
	 *          the childMetaMetadata to set
	 */
	public void setChildMetaMetadata(HashMapArrayList<String, MetaMetadataField> childMetaMetadata)
	{
		this.kids = childMetaMetadata;
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
		return (childMetaMetadata != null) ? new MetaMetadataFieldIterator() : EMPTY_ITERATOR;
	}

	public String key()
	{
		return name;
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

//	public HashMapArrayList<String, MetaMetadataField> initializeChildMetaMetadata()
//	{
//		this.kids = new HashMapArrayList<String, MetaMetadataField>();
//		return this.kids;
//	}

	/**
	 * check for errors.
	 * 
	 * @return true if nothing goes wrong; false if there is something wrong.
	 */
	@Deprecated
	abstract protected boolean checkForErrors();

	/**
	 * do we need to generate a new class definition (e.g. java source file) for this field? 
	 * 
	 * @return
	 */
	@Deprecated
	abstract public boolean isNewClass();

	/**
	 * is this a new declaration (instead of an inherited one), so that we should generate member
	 * declaration & getter/setters for this field?
	 * 
	 * @return
	 */
	@Deprecated
	protected boolean isNewDeclaration()
	{
		return getInheritedField() == null;
	}

	public MetaMetadataField lookupChild(MetadataFieldDescriptor metadataFieldDescriptor)
	{
		return lookupChild(XMLTools.getXmlTagName(metadataFieldDescriptor.getFieldName(), null));
	}

	public MetaMetadataField lookupChild(String name)
	{
		if (kids == null)
			throw new RuntimeException("Can't find child " + name + " in " + this + " <- " + this.parent());
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
	@Deprecated
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
		
		if (fieldToInheritTo instanceof MetaMetadataCollectionField)
		{
			MetaMetadataCompositeField childComposite = ((MetaMetadataCollectionField) fieldToInheritTo).getChildComposite();
			if (childComposite != null)
			{
				MetaMetadataCompositeField inheritedChildComposite = ((MetaMetadataCollectionField) fieldToInheritFrom).getChildComposite();
				
				if (MetaMetadataCollectionField.UNRESOLVED_NAME == childComposite.getName())
				{
					fieldToInheritTo.kids.remove(MetaMetadataCollectionField.UNRESOLVED_NAME);
					childComposite.inheritAttributes(inheritedChildComposite);
					childComposite.setName(inheritedChildComposite.getName());
					fieldToInheritTo.kids.put(childComposite.getName(), childComposite);
				}
			}
		}

		if (fieldToInheritTo == null)
		{
			MetaMetadataField clone;
			try
			{
				clone = (MetaMetadataField) fieldToInheritFrom.clone();
				clone.setParent(this);
				childMetaMetadata.put(fieldName, clone);
				fieldToInheritTo = clone;
			}
			catch (CloneNotSupportedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		else
		{			
			fieldToInheritTo.inheritAttributes(fieldToInheritFrom);
		}

		if (!fieldToInheritTo.fieldInherited)
		{
//			HashMapArrayList<String, MetaMetadataField> inheritedChildMetaMetadata = fieldToInheritFrom.getChildMetaMetadata();
//			if (inheritedChildMetaMetadata != null)
//			{
//				for (MetaMetadataField grandChildMetaMetadataField : inheritedChildMetaMetadata)
//				{
//					fieldToInheritTo.inheritForField(grandChildMetaMetadataField);
//				}
//			}

			fieldToInheritTo.fieldInherited = true;
		}
	}

	protected HashMapArrayList<String, MetaMetadataField> initializeChildMetaMetadata()
	{
		return null;
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
	
	/**
	 * translate a meta_metadata meta-language description into a Java class, in the form of source
	 * codes.
	 * 
	 * @param packageName
	 * @throws IOException
	 */
	public void compileToMetadataClass(String packageName) throws IOException
	{
//		if (!checkForErrors())
//			return;
		
		// create buffers
		StringBuilder memberDefinitions = new StringBuilder();
		StringBuilder methods = new StringBuilder();
		
		// loop to generate data member and method definitions
		HashMapArrayList<String, MetaMetadataField> metaMetadataFieldList = getChildMetaMetadata();
		if (metaMetadataFieldList != null)
		{
			for (MetaMetadataField metaMetadataField : metaMetadataFieldList)
			{
//				metaMetadataField.setExtendsField(extendsAttribute); // why?
				
				// set inherited properties
				metaMetadataField.setRepository(getRepository());
				
				if (metaMetadataField.isNewClass())
				{
					metaMetadataField.compileToMetadataClass(packageName);
					MetaMetadataCompilerUtils.importTargets.add(packageName + ".*");
				}
				
				if (metaMetadataField.isNewDeclaration())
				{
					try
					{
						metaMetadataField.compileToMemberDefinitions(memberDefinitions);
						metaMetadataField.compileToMethods(methods);
					}
					catch (IOException e)
					{
						warning("error in compiling " + this + ": " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
		
		// determine class name
		String className = XMLTools.classNameFromElementName(getTypeName());
		
		// open a file to write java source code
		String generationPath = MetaMetadataCompilerUtils.getGenerationPath(packageName);
		File directoryPath = PropertiesAndDirectories.createDirsAsNeeded(new File(generationPath));
		File srcFile = new File(directoryPath, className + ".java");
		System.out.println((this.file == null ? "" : this.file + "\n") + "\t\t -> " + srcFile + "\n");
		PrintWriter p = new PrintWriter(new FileWriter(srcFile));

		// update the translation class
		MetaMetadataCompilerUtils.appendToTranslationScope(packageName + "." + className + ".class,\n");

		// Write the package info
		p.println(MetaMetadataCompilerUtils.PACKAGE + " " + packageName + ";\n");

		// Write the import statements
		MetaMetadataCompiler.printImports(p);
		
		// Write java-doc comments
		String generalNotes = MetaMetadataCompilerUtils.COMMENT;
		String comment = getComment();
		if (comment == null)
			comment = "";
		MetaMetadataCompilerUtils.writeJavaDocComment(comment + generalNotes, p);

		// write annotation(s)
		p.println("@simpl_inherit");
		String tagDecl = getTagDecl();
		if (tagDecl != null && tagDecl.length() > 0)
			p.println(tagDecl);

		// Write class declaration
//		String extendsAttribute = getExtendsAttribute();
		String extendsAttribute = null;
		String extendsName = extendsAttribute == null ? "Metadata" : XMLTools.classNameFromElementName(extendsAttribute);
		p.println("public class " + className + "\nextends " + extendsName + "\n{\n");

		// data members
		p.println(memberDefinitions.toString());
		
		// write the constructors
		MetaMetadataCompilerUtils.appendBlankConstructor(p, className);
		MetaMetadataCompilerUtils.appendConstructor(p, className);
		
		// other methods
		p.println(methods.toString());
			
		// end the class declaration
		p.println("\n}\n");
		p.flush();
		p.close();
	}

	/**
	 * compile this field into a member definition.
	 * 
	 * @param appendable
	 * @param packageName
	 * @throws IOException
	 */
	public void compileToMemberDefinitions(Appendable appendable) throws IOException
	{
		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);
		if (!dontSerialize())
		{
			appendable.append(getAnnotationsInJava());
			appendable.append("\n");
		}
		appendable.append("private ");
		appendable.append(getTypeNameInJava());
		appendable.append("\t");
		appendable.append(getFieldNameInJava(false));
		appendable.append(";\n\n");
	}
	
	@Deprecated
	abstract public String getAnnotationsInJava();
	
	abstract public String getAdditionalAnnotationsInJava();

	private String fieldNameInJava = null;
	private String capFieldNameInJava = null;
	
	// TODO change getFieldName() to this one; add javadoc
	protected String getFieldNameInJava(boolean capitalized)
	{
		if (capitalized)
			return getCapFieldNameInJava();
		
		String rst = fieldNameInJava;
		if (rst == null)
		{
			rst = XMLTools.fieldNameFromElementName(getName());
			fieldNameInJava = rst;
		}
		return fieldNameInJava;
	}
	
	private String getCapFieldNameInJava()
	{
		String rst = capFieldNameInJava;
		if (rst == null)
		{
			rst = XMLTools.javaNameFromElementName(getName(), true);
			capFieldNameInJava = rst;
		}
		return capFieldNameInJava;
	}

	/**
	 * generate java type name string. since type name will be used for several times (both in member
	 * definition and methods), it should be cached.
	 * 
	 * note that this could be different from changing getTypeName() into camel case: consider Entity
	 * for composite fields or ArrayList for collection fields.
	 */
	abstract protected String getTypeNameInJava();
	
	/**
	 * compile this field into methods. typical methods include lazy evaluation methid, getter and
	 * setter. override to add more methods.
	 * 
	 * @param appendable
	 * @param packageName
	 * @throws IOException 
	 */
	public void compileToMethods(Appendable appendable) throws IOException
	{
		compileLazyEvaluationMethod(appendable);
		compileGetter(appendable);
		compileSetter(appendable);
	}
	
	/**
	 * generate a lazy evaluation method for this field.
	 * 
	 * @param appendable
	 * @throws IOException
	 */
	protected void compileLazyEvaluationMethod(Appendable appendable) throws IOException
	{
		String fieldName = getFieldNameInJava(false);
		String typeName = getTypeNameInJava();
		
		// write comment for this method
		String comment = "Lazy evaluation for " + fieldName;
		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);
	
		// first line. Start of method name
		appendable.append("public ").append(typeName).append("\t").append(fieldName).append("()\n{\n");
	
		// second line. Declaration of result variable.
		appendable.append("\t");
		appendable.append(typeName).append("\t").append("result = this.").append(fieldName).append(";\n");
	
		// third line. Start of if statement
		appendable.append("\t");
		appendable.append("if (result == null)\n\t{\n");
	
		// fourth line. creation of new result object.
		appendable.append("\t\t");
		appendable.append("result = new ").append(typeName).append("();\n");
	
		// fifth line. end of if statement
		appendable.append("\t\t");
		appendable.append("this.").append(fieldName).append(" = result;\n\t}\n");
	
		// sixth line. return statement and end of method.
		appendable.append("\t");
		appendable.append("return result;\n}\n");
	}

	/**
	 * generate a getter for this field.
	 * 
	 * @param appendable
	 * @throws IOException
	 */
	protected void compileGetter(Appendable appendable) throws IOException
	{
		String fieldName = getFieldNameInJava(false);
		String typeName = getTypeNameInJava();
		
		// write Java doc
		String comment = "Get the value of field " + fieldName;
		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		appendable.append("public ").append(typeName).append(" get")
							.append(getFieldNameInJava(true)).append("()\n{\n");
		appendable.append("\treturn this.").append(fieldName).append(";\n}\n");
	}

	/**
	 * generate a setter for this field.
	 * 
	 * @param appendable
	 * @throws IOException
	 */
	protected void compileSetter(Appendable appendable) throws IOException
	{
		String fieldName = getFieldNameInJava(false);
		String typeName = getTypeNameInJava();
		
		// write Java doc
		String comment = "Set the value of field " + fieldName;
		MetaMetadataCompilerUtils.writeJavaDocComment(comment, appendable);

		// write first line
		appendable.append("public void set").append(getFieldNameInJava(true))
		          .append("(").append(typeName).append(" ").append(fieldName).append(")\n{\n");
		appendable.append("\tthis.").append(fieldName).append(" = ").append(fieldName).append(";\n}\n");
	}

	public void inheritAttributes(MetaMetadataField inheritFrom)
	{
		MetaMetadataClassDescriptor classDescriptor = (MetaMetadataClassDescriptor) classDescriptor();
	
		for (MetaMetadataFieldDescriptor fieldDescriptor : classDescriptor)
		{
			if (fieldDescriptor.isInheritable())
			{
				ScalarType scalarType = fieldDescriptor.getScalarType();
				try
				{
					if (scalarType != null
							&& scalarType.isDefaultValue(fieldDescriptor.getField(), this)
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
					debug(inheritFrom.getName() + " doesn't have field " + fieldDescriptor.getFieldName() + ", ignore it.");
//					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
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
		if (!bindDescriptorsFinished)
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
						if(fd != null)
							fd.setRegexFilter(Pattern.compile(scalar.getRegexPattern()), scalar.getRegexReplacement());
						else
							warning("Encountered null fd for scalar: " + scalar);
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
				
				bindDescriptorsFinished = true;
				
				if (this instanceof MetaMetadata)
				{
					MetaMetadata mmd = (MetaMetadata) this;
					String naturalId = thatChild.getAsNaturalId();
					if (naturalId != null)
					{
						mmd.addNaturalIdField(naturalId, thatChild);
					}
				}
			}
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
					metadataClassDescriptor = (MetadataClassDescriptor) ClassDescriptor.getClassDescriptor(metadataClass);
					bindMetadataFieldDescriptors(metadataTScope, metadataClassDescriptor);
					this.metadataClassDescriptor = metadataClassDescriptor;
				}
			}
		}
	}

	void bindMetadataFieldDescriptor(TranslationScope metadataTScope,
			MetadataClassDescriptor metadataClassDescriptor)
	{
		String fieldName = this.getFieldNameInJava(false); // TODO -- is this the correct tag?
		MetadataFieldDescriptor metadataFieldDescriptor = (MetadataFieldDescriptor) metadataClassDescriptor
				.getFieldDescriptorByFieldName(fieldName);
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
			warning("Ignoring <" + fieldName + "> because no corresponding MetadataFieldDescriptor can be found.");
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
	 * get the type name of this field, in terms of meta-metadata.
	 * 
	 * TODO redefining this.
	 * 
	 * @return the type name.
	 */
	abstract protected String getTypeName();

	/**
	 * get the meta-metadata (or meta-metadata field) defining the type of this field.
	 * 
	 * @return as above explained.
	 */
	abstract protected MetaMetadataNestedField getTypeDefinition();
	
	/**
	 * @return the meta-metadata field object from which this field inherits.
	 */
	public MetaMetadataField getInheritedField()
	{
		return inheritedField;
	}
	
	void setInheritedField(MetaMetadataField inheritedField)
	{
		debug("setting " + this + ".inheritedField to " + inheritedField);
		this.inheritedField = inheritedField;
	}
	
	public int getFieldType()
	{
		if (this.metadataFieldDescriptor != null)
			return metadataFieldDescriptor.getType();
		else
		{
			if (this instanceof MetaMetadataCompositeField)
				return FieldTypes.COMPOSITE_ELEMENT;
			else if (this instanceof MetaMetadataCollectionField)
			{
				MetaMetadataCollectionField coll = (MetaMetadataCollectionField) this;
				if (coll.getChildScalarType() != null)
					return FieldTypes.COLLECTION_SCALAR;
				return FieldTypes.COLLECTION_ELEMENT;
			}
			else
				return FieldTypes.SCALAR;
		}
	}

	public String getAsNaturalId()
	{
		return asNaturalId;
	}
	
	public boolean isRequired()
	{
		return required;
	}
	
	public String getFormat()
	{
		return format;
	}
	
	public boolean dontSerialize()
	{
		return dontSerialize;
	}

	public String getSchemaOrgItemprop()
	{
		return schemaOrgItemprop;
	}
	
	class MetaMetadataFieldIterator implements Iterator<MetaMetadataField>
	{
		int currentIndex = 0;

		@Override
		public boolean hasNext()
		{
			int size 				= kids.size();
			boolean result 	= currentIndex < size;
			
			if (result)
			{
				for(int i=currentIndex; i < size; i++)
				{
					MetaMetadataField nextField = kids.get(i);
					if (nextField.isIgnoreCompletely())
					{
						currentIndex++;
					}
					else
						break;
				}
				
				if (currentIndex == size)
					result = false;
			}
			
			return result;
		}

		@Override
		public MetaMetadataField next()
		{
			return kids.get(currentIndex++);
		}

		@Override
		public void remove()
		{
			// TODO Auto-generated method stub
			
		}
		
	}

	public MetaMetadata getDeclaringMmd()
	{
		return declaringMmd;
	}

	void setDeclaringMmd(MetaMetadata declaringMmd)
	{
		this.declaringMmd = declaringMmd;
	}

	public ArrayList<String> getOtherTags()
	{
		return otherTags;
	}

	void addOtherTag(String otherTag)
	{
		if (this.getInheritedField() != null)
		{
			this.getInheritedField().addOtherTag(otherTag);
			return;
		}
		
		if (this.otherTags == null)
			this.otherTags = new ArrayList<String>();
		this.otherTags.add(otherTag);
	}

	abstract public MetadataFieldDescriptor findOrGenerateMetadataFieldDescriptor(MetadataClassDescriptor contextCd);
	
}
