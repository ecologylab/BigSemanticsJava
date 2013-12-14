package ecologylab.bigsemantics.metametadata;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
import ecologylab.generic.HashMapArrayList;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.MetaInformation;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_descriptor_classes;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_map;
import ecologylab.serialization.annotations.simpl_map_key_field;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.annotations.simpl_wrap;
import ecologylab.serialization.types.ScalarType;
import ecologylab.serialization.types.element.IMappable;
import ecologylab.textformat.NamedStyle;

/**
 * The basic meta-metadata field class. Encapsulate common attributes and methods for all types of
 * meta-metadata fields.
 * 
 * @author damaraju
 * 
 */
@SuppressWarnings("rawtypes")
@simpl_inherit
@simpl_descriptor_classes({ MetaMetadataClassDescriptor.class, MetaMetadataFieldDescriptor.class })
public abstract class MetaMetadataField extends ElementState
implements IMappable<String>, Iterable<MetaMetadataField>, MMDConstants, Cloneable
{

	/**
	 * the Comparator for conveniently sort fields.
	 */
	static class LayerComparator implements Comparator<MetaMetadataField>
	{

		@Override
		public int compare(MetaMetadataField o1, MetaMetadataField o2)
		{
			// return negation for descending ordering in sort
			return -Float.compare(o1.layer, o2.layer);
		}

	}

	/**
	 * the Iterator class of fields inside this one.
	 */
	// FIXME move this to NestedField.
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

	static LayerComparator																LAYER_COMPARATOR				= new LayerComparator();

	static ArrayList<MetaMetadataField>										EMPTY_COLLECTION				= new ArrayList<MetaMetadataField>(0);

	static Iterator<MetaMetadataField>										EMPTY_ITERATOR					= EMPTY_COLLECTION.iterator();

	MetadataFieldDescriptor																metadataFieldDescriptor;

	@simpl_scalar
	protected String																			name;

	@simpl_scalar
	@mm_dont_inherit
	protected String																			comment;

	@simpl_scalar
	protected String																			tag;

	@simpl_scalar
	private String																				otherTags;

	@simpl_scalar
	protected String																			xpath;
	
	@simpl_scalar
	private boolean																				extractAsHtml						=	false;

	/**
	 * Context node for xpath based extarction rules for this field. Default value is document root.
	 */
	@mm_dont_inherit
	@simpl_scalar
	protected String																			contextNode;

	/**
	 * used in the field_parser mechanism, which takes a string as input and parse it into values
	 * indexed by keys. this field indicates what key this field uses to decide the value for it
	 * inside a field_parser.
	 */
	@simpl_scalar
	protected String																			fieldParserKey;

	/**
	 * schema.org microdata item_prop name.
	 */
	@simpl_scalar
	private String																				schemaOrgItemprop;

	/**
	 * if this field should be lazily evaluated in ORM component. note that nested fields are
	 * automatically lazy.
	 */
	@simpl_scalar
	protected boolean																			ormLazy;

	/**
	 * if this field should be indexed in database representation, used by the ORM component.
	 */
	@simpl_scalar
	protected boolean																			indexed;

	/**
	 * the nested structure inside this field.
	 */
	// initializing kids here seems a waste of space, but I would argue for this because this field
	// will get created during the inheritance process anyway. -- yin
	// FIXME move this to NestedField.
	@simpl_map
	@simpl_scope(NestedMetaMetadataFieldTypesScope.NAME)
	@simpl_nowrap
	protected HashMapArrayList<String, MetaMetadataField>	kids										= new HashMapArrayList<String, MetaMetadataField>();

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
	@mm_dont_inherit
	protected boolean																			alwaysShow;

	/**
	 * name of a style.
	 */
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

	/**
	 * if this field should be serialized.
	 */
	@simpl_scalar
	protected boolean																			dontSerialize						= false;

	// ///////////////////////////////// switches /////////////////////////////////

	/**
	 * if this field is used as a facet.
	 */
	@simpl_scalar
	protected boolean																			isFacet;

	/**
	 * if we should ignore this in the term vector.
	 */
	@simpl_scalar
	protected boolean																			ignoreInTermVector;

	/**
	 * if we should ignore this field completely (which will cause ignoring of this field for both
	 * display and term vector).
	 */
	@simpl_scalar
	protected boolean																			ignoreCompletely;

	@simpl_composite
	protected RegexFilter																	filter;
	
	@simpl_map("generic_type_var")
	@simpl_map_key_field("name")
	@simpl_nowrap
	MmdGenericTypeVarScope																genericTypeVars;
	
	// ///////////////////////////////// members /////////////////////////////////

	HashSet<String>																				nonDisplayedFieldNames;

	private boolean																				fieldsSortedForDisplay	= false;

	private String																				displayedLabel					= null;

	/**
	 * The (global) Meta-Metadata repository.
	 */
	private MetaMetadataRepository												repository;
	
	@simpl_scalar
	@mm_dont_inherit
	protected boolean																			inheritFinished					= false;

	/**
	 * inheritInProcess prevents infinite loops, e.g. when A.b refers B while B.a refers A, then when
	 * you initialize A.b you will have to initialize A.b.a and you will have to initialize A.b.a.b
	 * ...
	 */
	protected boolean																			inheritInProcess				= false;

	/**
	 * Class of the Metadata object that corresponds to this. Non-null for nested and collection
	 * fields. Null for scalar fields.
	 */
	protected Class<? extends Metadata>										metadataClass;

	/**
	 * Class descriptor for the Metadata object that corresponds to this. Non-null for nested and
	 * collection fields. Null for scalar fields.
	 */
	protected MetadataClassDescriptor											metadataClassDescriptor;

	/**
	 * (for caching toString())
	 */
	String																								toString;

	/**
	 * from which field this one inherits. could be null if this field is declared for the first time.
	 */
	@simpl_composite
  @simpl_scope(NestedMetaMetadataFieldTypesScope.NAME)
	@simpl_wrap
	@mm_dont_inherit
	private MetaMetadataField															inheritedField					= null;

	/**
	 * in which meta-metadata this field is declared.
	 */
	@simpl_composite
	@mm_dont_inherit
	private MetaMetadata																	declaringMmd						= null;
	
	/**
	 * if this field is used to define inline meta-metadata types. this flag is used by extraction
	 * module to determine the true root element for child fields inside this field.
	 */
	@simpl_scalar
	@mm_dont_inherit
	private boolean																				usedForInlineMmdDef			= false;
	
	/**
	 * hint for renderer to not label the extracted value in presentation
	 */
	@simpl_scalar
	private boolean																				hideLabel;
	
	/**
	 * Another field name whose value can be used as label for this field
	 */
	@simpl_scalar
	protected String																			useValueAsLabel;
	
	/**
	 * hint for renderer to concatenate this field to another 
	 */
	@simpl_scalar
	protected String																			concatenatesTo;
	
	/**
	 * hint for renderer how to position label w.r.t. value
	 */
	@simpl_scalar
	protected String																			labelAt;

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
	
	/**
	 * test if two fields are equal. equal fields are either the same one or inherited from the same
	 * origin.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof MetaMetadataField)
		{
			MetaMetadataField f = (MetaMetadataField) obj;
			if (f.getName().equals(this.getName()) && f.getDeclaringMmd() == this.getDeclaringMmd())
				return true;
		}
		return false;
	}

	/**
	 * get the nested fields inside of this one.
	 * 
	 * @return
	 */
	public HashMapArrayList<String, MetaMetadataField> getChildMetaMetadata()
	{
		return kids;
	}

	/**
	 * comment of this field.
	 * 
	 * @return
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * context node xpath (used in extraction).
	 * 
	 * @return
	 */
	public final String getContextNode()
	{
		return contextNode;
	}

	/**
	 * the label used to display this field in visualization.
	 * 
	 * @return
	 */
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
	 * the key used by this field to index a value in the output of a field_parser.
	 * 
	 * @return
	 */
	public String getFieldParserKey()
	{
		return fieldParserKey;
	}

	/**
	 * the file in which this field is declared.
	 * 
	 * @return
	 */
	public File getFile()
	{
		MetaMetadataField parent = (MetaMetadataField) parent();
		if (parent == null)
			return null;
		if (parent instanceof MetaMetadata)
			return ((MetaMetadata) parent).getFile();
		return parent.getFile();
	}

	/**
	 * @return the corresponding metadata class. should be used after inheritance and binding process.
	 *         null for scalars.
	 */
	public Class<? extends Metadata> getMetadataClass()
	{
		Class<? extends Metadata> metadataClass = this.metadataClass;
		if (metadataClass == null)
		{
			MetadataClassDescriptor metadataClassDescriptor = this.getMetadataClassDescriptor();
			metadataClass = (Class<? extends Metadata>) (metadataClassDescriptor == null ? null : metadataClassDescriptor.getDescribedClass());
			this.metadataClass = metadataClass;
		}
		return metadataClass;
	}

	/**
	 * @return the corresponding metadataClassDescriptor. null for scalars.
	 *         <p>
	 *         note that this class descriptor might be incomplete during the compilation process,
	 *         e.g. lacking the actual Class.
	 */
	public MetadataClassDescriptor getMetadataClassDescriptor()
	{
		return metadataClassDescriptor;
	}
	
	/**
	 * @return the corresponding metadataFieldDescriptor.
	 */
	public MetadataFieldDescriptor getMetadataFieldDescriptor()
	{
		return metadataFieldDescriptor;
	}
	
	/**
	 * 
	 * @return the style name.
	 */
	public String getStyle()
	{
		return style;
	}

	/**
	 * 
	 * @return name of this field.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * 
	 * @return the link target (should be another ParsedURL field) of this field.
	 */
	public String getNavigatesTo()
	{
		return navigatesTo;
	}

	/**
	 * 
	 * @return the meta-metadata repository in which this field is declared.
	 */
	public MetaMetadataRepository getRepository()
	{
		if (repository == null)
			repository = findRepository();
		return repository;
	}

	/**
	 * 
	 * @return the tag if existed, or the name of this field. this method is different from
	 * getTagForTypesScope() which is overridden in MetaMetadataCollectionField. they have
	 * different purposes.
	 */
	public String getTagOrName()
	{
		return tag != null ? tag : name;
	}
	
	/**
	 * 
	 * @return the tag used to look up metadata from MetadataTypesScope.
	 */
	public String getTagForTypesScope()
	{
		return tag != null ? tag : name;
	}

	public String getType()
	{
		return null;
	}

	public String getExtendsAttribute()
	{
		return null;
	}
	/**
	 * the xpath of this field.
	 * @return
	 */
	public String getXpath()
	{
		return xpath;
	}

	/**
	 * if this field should always be shown in visualization.
	 * @return
	 */
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
		StringBuilder result = new StringBuilder();
	
		ElementState parent = parent();
		while (parent instanceof MetaMetadataField)
		{
			MetaMetadataField pf = (MetaMetadataField) parent;
			result.insert(0, "<" + pf.name + ">");
			parent = parent.parent();
		}
		return result.toString();
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

	@Override
	public Iterator<MetaMetadataField> iterator()
	{
		HashMapArrayList<String, MetaMetadataField> childMetaMetadata = getChildMetaMetadata();
		return (childMetaMetadata != null) ? new MetaMetadataFieldIterator() : EMPTY_ITERATOR;
	}

	@Override
	public String key()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		String result = toString;
		if (result == null)
		{
			result = getClassSimpleName() + parentString() + "<" + name + ">";
			toString = result;
		}
		return result;
	}

//	public HashMapArrayList<String, MetaMetadataField> initializeChildMetaMetadata()
//	{
//		this.kids = new HashMapArrayList<String, MetaMetadataField>();
//		return this.kids;
//	}

	public MetaMetadataField lookupChild(MetadataFieldDescriptor metadataFieldDescriptor)
	{
		return lookupChild(XMLTools.getXmlTagName(metadataFieldDescriptor.getName(), null));
	}

	public MetaMetadataField lookupChild(String name)
	{
		if (kids == null)
			throw new RuntimeException("Can't find child " + name + " in " + this + " <- " + this.parent());
		return kids.get(name);
	}

	public NamedStyle lookupStyle()
	{
		NamedStyle result = null;
		if (style != null)
			result = getRepository().lookupStyle(style);
		return (result != null) ? result : getRepository().getDefaultStyle();
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

	protected HashMapArrayList<String, MetaMetadataField> initializeChildMetaMetadata()
	{
		return null;
	}

	protected HashSet<String> nonDisplayedFieldNames()
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
	 * Add additional meta information about the field to an existing meta information list.
	 * 
	 * @param metaInfoBuf
	 *          The existing meta information list. Additional meta information will be added to this
	 *          list. Cannot be null.
	 * @param compiler
	 *          Providing compiler services such as dependency handling, in case needed in this
	 *          method. This is not used right now; just for extensibility.
	 */
	abstract public void addAdditionalMetaInformation(List<MetaInformation> metaInfoBuf, MmdCompilerService compiler);

	private String fieldNameInJava = null;
	private String capFieldNameInJava = null;
	
	/**
	 * get the field name in java.
	 * 
	 * @param capitalized
	 * @return
	 */
	public String getFieldNameInJava(boolean capitalized)
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
	
	/**
	 * get the capitalized field name in java (could be used in method names).
	 * 
	 * @return
	 */
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
	
	public void inheritAttributes(MetaMetadataField inheritFrom)
	{
		MetaMetadataClassDescriptor classDescriptor = (MetaMetadataClassDescriptor)  ClassDescriptor.getClassDescriptor(this);;
	
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
//						debug("inherit\t" + this.getName() + "." + fieldDescriptor.getFieldName() + "\t= "
//								+ value);
					}
				}
				catch (IllegalArgumentException e)
				{
					debug(inheritFrom.getName() + " doesn't have field " + fieldDescriptor.getName() + ", ignore it.");
//					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
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
	 * bind the corresponding metadata field descriptor to this field, matched by field name.
	 * customize the field descriptor when needed.
	 * <p>
	 * note that the customization assumes that the field descriptor is copied from super class to
	 * this class. if this changes in the future, the customization process should do this copy. see
	 * {@code customizeFieldDescriptor()}.
	 * <p>
	 * lazy evaluation. result cached.
	 * 
	 * @param metadataTScope
	 *          the translation scope of (generated) metadata classes.
	 * @param metadataClassDescriptor
	 *          the current metadata class descriptor.
	 * @return the bound metadata field descriptor if any.
	 * 
	 * @see {@code customizeFieldDescriptor()}
	 */
	protected MetadataFieldDescriptor bindMetadataFieldDescriptor(SimplTypesScope metadataTScope, MetadataClassDescriptor metadataClassDescriptor)
	{
		MetadataFieldDescriptor metadataFieldDescriptor = this.metadataFieldDescriptor;
		if (metadataFieldDescriptor == null)
		{
			synchronized (this)
			{
				metadataFieldDescriptor = this.metadataFieldDescriptor;
				String fieldName = this.getFieldNameInJava(false);
				if (metadataFieldDescriptor == null)
				{
					metadataFieldDescriptor = (MetadataFieldDescriptor) metadataClassDescriptor.getFieldDescriptorByFieldName(fieldName);
					if (metadataFieldDescriptor != null)
					{
						// if we don't have a field, then this is a wrapped collection, so we need to get the
						// wrapped field descriptor
						if (metadataFieldDescriptor.getField() == null)
							metadataFieldDescriptor = (MetadataFieldDescriptor) metadataFieldDescriptor.getWrappedFD();

						this.metadataFieldDescriptor = metadataFieldDescriptor;
						
						// this method handles polymorphic type / changing tags
						if (this.metadataFieldDescriptor != null)
							customizeFieldDescriptor(metadataTScope, fieldDescriptorProxy);
						if (this.metadataFieldDescriptor != metadataFieldDescriptor)
						{
							// the field descriptor has been modified in customizeFieldDescriptor()!
							// we need to update it in the class descriptor so that deserialization of metadata
							// objects can work correctly, e.g. using the right classDescriptor for a composite
							// field or a right elementClassDescriptor for a collection field.
							customizeFieldDescriptorInClass(metadataTScope, metadataClassDescriptor);
						}
					}
				}
				else
				{
					warning("Ignoring <" + fieldName + "> because no corresponding MetadataFieldDescriptor can be found.");
				}
			}
		}
		return metadataFieldDescriptor;
	}

	private void customizeFieldDescriptorInClass(SimplTypesScope metadataTScope, MetadataClassDescriptor metadataClassDescriptor)
	{
		MetadataFieldDescriptor oldFD = metadataClassDescriptor.getFieldDescriptorByFieldName(this.getFieldNameInJava(false)); // oldFD is the non-wrapper one
		String newTagName = this.metadataFieldDescriptor.getTagName();
		
		metadataClassDescriptor.replace(oldFD, this.metadataFieldDescriptor);
		
		MetadataFieldDescriptor wrapperFD = (MetadataFieldDescriptor) this.metadataFieldDescriptor.getWrapper();
		if (wrapperFD != null)
		{
			MetadataFieldDescriptor clonedWrapperFD = wrapperFD.clone();
			clonedWrapperFD.setTagName(newTagName);
			clonedWrapperFD.setWrappedFD(this.metadataFieldDescriptor);
			metadataClassDescriptor.replace(wrapperFD, clonedWrapperFD);
		}
		
		FieldType fieldType = this.metadataFieldDescriptor.getType();
		if (fieldType == FieldType.COLLECTION_ELEMENT || fieldType == FieldType.MAP_ELEMENT)
		{
			if (!this.metadataFieldDescriptor.isWrapped())
			{
				String childTagName = this.metadataFieldDescriptor.getCollectionOrMapTagName();
				oldFD = metadataClassDescriptor.getFieldDescriptorByTag(childTagName, metadataTScope);
				metadataClassDescriptor.replace(oldFD, this.metadataFieldDescriptor);
			}
		}
	}

	/**
	 * this class encapsulate the clone-on-write behavior of metadata field descriptor associated
	 * with this field.
	 * 
	 * @author quyin
	 *
	 */
	protected class MetadataFieldDescriptorProxy
	{
		
		boolean fieldDescriptorCloned = false;

		private void cloneFieldDescriptorOnWrite()
		{
			if (!fieldDescriptorCloned)
			{
				MetaMetadataField.this.metadataFieldDescriptor = MetaMetadataField.this.metadataFieldDescriptor.clone();
				fieldDescriptorCloned = true;
			}
		}

		public void setTagName(String newTagName)
		{
			if (newTagName != null && !newTagName.equals(MetaMetadataField.this.metadataFieldDescriptor.getTagName()))
			{
				cloneFieldDescriptorOnWrite();
				MetaMetadataField.this.metadataFieldDescriptor.setTagName(newTagName);
			}
		}

		public void setElementClassDescriptor(MetadataClassDescriptor metadataClassDescriptor)
		{
			if (metadataClassDescriptor != MetaMetadataField.this.metadataFieldDescriptor.getElementClassDescriptor())
			{
				cloneFieldDescriptorOnWrite();
				MetaMetadataField.this.metadataFieldDescriptor.setElementClassDescriptor(metadataClassDescriptor);
			}
		}

		public void setCollectionOrMapTagName(String childTag)
		{
			if (childTag != null && !childTag.equals(MetaMetadataField.this.metadataFieldDescriptor.getCollectionOrMapTagName()))
			{
				cloneFieldDescriptorOnWrite();
				MetaMetadataField.this.metadataFieldDescriptor.setCollectionOrMapTagName(childTag);
			}
		}

		public void setWrapped(boolean wrapped)
		{
			if (wrapped != MetaMetadataField.this.metadataFieldDescriptor.isWrapped())
			{
				cloneFieldDescriptorOnWrite();
				MetaMetadataField.this.metadataFieldDescriptor.setWrapped(wrapped);
			}
		}
		
	}
	
	private MetadataFieldDescriptorProxy fieldDescriptorProxy = new MetadataFieldDescriptorProxy();
	
	/**
	 * this method customizes field descriptor for this field, e.g. specific type or tag.
	 * 
	 * @param metadataTScope
	 *          the translation scope of (generated) metadata classes.
	 * @param fdProxy
	 *          the current metadata field descriptor.
	 */
	protected void customizeFieldDescriptor(SimplTypesScope metadataTScope, MetadataFieldDescriptorProxy fdProxy)
	{
		fdProxy.setTagName(this.getTagOrName());
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
	 * @return the meta-metadata field object from which this field inherits.
	 */
	public MetaMetadataField getInheritedField()
	{
		return inheritedField;
	}
	
	void setInheritedField(MetaMetadataField inheritedField)
	{
//		debug("setting " + this + ".inheritedField to " + inheritedField);
		this.inheritedField = inheritedField;
	}
	
	public FieldType getFieldType()
	{
		if (this.metadataFieldDescriptor != null)
			return metadataFieldDescriptor.getType();
		else
		{
			if (this instanceof MetaMetadataCompositeField)
				return FieldType.COMPOSITE_ELEMENT;
			else if (this instanceof MetaMetadataCollectionField)
			{
				MetaMetadataCollectionField coll = (MetaMetadataCollectionField) this;
				if (coll.getChildScalarType() != null)
				{
					return FieldType.COLLECTION_SCALAR;
				}
				else
				{
					return FieldType.COLLECTION_ELEMENT;
				}
			}
			else
			{
				return FieldType.SCALAR;
			}
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
	
	public MetaMetadata getDeclaringMmd()
	{
		return declaringMmd;
	}

	void setDeclaringMmd(MetaMetadata declaringMmd)
	{
		this.declaringMmd = declaringMmd;
	}

/**
 * 
 * @param deserializationMM
 * @return	true if binding succeeds
 */
	public boolean validateMetaMetadataToMetadataBinding(MetaMetadataField deserializationMM)
	{
		if (deserializationMM != null) // should be always
		{
			MetadataClassDescriptor originalClassDescriptor = this.getMetadataClassDescriptor();
			MetadataClassDescriptor deserializationClassDescriptor = deserializationMM
					.getMetadataClassDescriptor();

			// quick fix for a NullPointerException for RSS. originalClassDescriptor can be null because
			// it might be a meta-metadata that does not generate metadata class, e.g. xml
			if (originalClassDescriptor == null)
				return true; // use the one from deserialization

			boolean sameMetadataSubclass = originalClassDescriptor.equals(deserializationClassDescriptor);
			// if they have the same metadataClassDescriptor, they can be of the same type, or one
			// of them is using "type=" attribute.
			boolean useMmdFromDeserialization = sameMetadataSubclass && (deserializationMM.getType() != null);
			if (!useMmdFromDeserialization && !sameMetadataSubclass)
				// if they have different metadataClassDescriptor, need to choose the more specific one
				useMmdFromDeserialization = originalClassDescriptor.getDescribedClass().isAssignableFrom(
						deserializationClassDescriptor.getDescribedClass());
			return useMmdFromDeserialization;
		}
		else
		{
			error("No meta-metadata in root after direct binding :-(");
			return false;
		}
	}
	
	abstract public MetadataFieldDescriptor findOrGenerateMetadataFieldDescriptor(SimplTypesScope tscope, MetadataClassDescriptor contextCd);

	public String getOtherTags()
	{
		return otherTags;
	}
	public String getSchemaOrgItemtype()
	{
		return null;
	}

	public boolean isExtractAsHtml()
	{
		return extractAsHtml;
	}

	public void setExtractAsHtml(boolean extractAsHtml)
	{
		this.extractAsHtml = extractAsHtml;
	}

	public boolean isAuthoredChildOf(MetaMetadataField parentField)
	{
		if (parentField instanceof MetaMetadataCompositeField && this.parent() == parentField)
			return true;
		if (parentField instanceof MetaMetadataCollectionField
				&& this.parent() == ((MetaMetadataCollectionField) parentField).getChildComposite())
			return true;
		return false;
	}

	public boolean isUsedForInlineMmdDef()
	{
		return usedForInlineMmdDef;
	}

	public void setUsedForInlineMmdDef(boolean usedForInlineMmdDef)
	{
		this.usedForInlineMmdDef = usedForInlineMmdDef;
	}

	/**
	 * @return the regex pattern
	 */
	public Pattern getRegexPattern()
	{
		if (filter != null)
			return filter.getRegex();
		return null;
	}
	
	public int getRegexGroup()
	{
		if (filter != null)
			return filter.getGroup();
		return 0;
	}

	/**
	 * @return the replacement string
	 */
	public String getRegexReplacement()
	{
		if (filter != null)
			return filter.getReplace();
		return null;
	}
	
	public boolean isNormalizeText()
	{
		return filter == null ? false : filter.isNormalizeText();
	}
	
	public MmdGenericTypeVarScope getMetaMetadataGenericTypeVarScope()
	{
		return genericTypeVars;
	}
	
	static Collection<MmdGenericTypeVar> EMPTY_GENERIC_TYPE_VAR_COLLECTION = new ArrayList<MmdGenericTypeVar>();
	
	public Collection<MmdGenericTypeVar> getMetaMetadataGenericTypeVars()
	{
		return genericTypeVars == null ? EMPTY_GENERIC_TYPE_VAR_COLLECTION : genericTypeVars.values();
	}

	public boolean isHideLabel() {
		return hideLabel;
	}

	public String getUseValueAsLabel() {
		return useValueAsLabel;
	}

	public String getConcatenatesTo() {
		return concatenatesTo;
	}

	public String getLabelAt() {
		return labelAt;
	}
	
}
