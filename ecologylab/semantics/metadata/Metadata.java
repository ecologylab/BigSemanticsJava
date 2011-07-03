package ecologylab.semantics.metadata;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionHandler;
import ecologylab.semantics.collecting.LinkedMetadataMonitor;
import ecologylab.semantics.metadata.builtins.DebugMetadata;
import ecologylab.semantics.metadata.scalar.MetadataParsedURL;
import ecologylab.semantics.metadata.scalar.MetadataString;
import ecologylab.semantics.metametadata.ClassAndCollectionIterator;
import ecologylab.semantics.metametadata.LinkWith;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataOneLevelNestingIterator;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.CompositeTermVector;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.semantics.model.text.TermVectorFeature;
import ecologylab.semantics.seeding.SearchState;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.tools.MetaMetadataCompilerUtils;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.simpl_descriptor_classes;
import ecologylab.serialization.library.html.Div;
import ecologylab.serialization.library.html.Input;
import ecologylab.serialization.library.html.Span;
import ecologylab.serialization.library.html.Table;
import ecologylab.serialization.library.html.Td;
import ecologylab.serialization.library.html.Tr;

/**
 * This is the new metadata class that is the base class for the meta-metadata system. It contains
 * all the functionality of the previous Metadata, and MetadataField classes.
 * 
 * Classes will extend this base class to provide a nested metadata structure
 * 
 * @author sashikanth
 * 
 */
@simpl_descriptor_classes(
{ MetadataClassDescriptor.class, MetadataFieldDescriptor.class })
abstract public class Metadata extends ElementState implements MetadataBase, TermVectorFeature,
		Iterable<MetadataFieldDescriptor>
{
	@simpl_scalar
//	@xml_other_tags("meta_metadata_name")
	@xml_tag("mm_name")
	MetadataString												metaMetadataName;

	// FIXME with the new inheritance and inline MMD processing, this will always be a MetaMetadata!
	private MetaMetadataCompositeField		metaMetadata;

	/**
	 * Allows combining instantiated Metadata subclass declarations without hierarchy.
	 * 
	 * Could help, for example, to support user annotation.
	 */
	@semantics_mixin
	@simpl_collection
	@simpl_scope(MetaMetadataCompilerUtils.GENERATED_METADATA_TRANSLATIONS)
	@mm_name("mixins")
	ArrayList<Metadata>										mixins;

	/**
	 * Hidden reference to the MetaMetadataRepository. DO NOT access this field directly. DO NOT
	 * create a static public accessor. -- andruid 10/7/09.
	 */
	private static MetaMetadataRepository	repository;

	private static final String						MIXINS_FIELD_NAME					= "mixins";

	protected CompositeTermVector					termVector								= null;

	final static int											INITIAL_SIZE							= 5;

	/**
	 * Indicates whether or not metadata has changed since last displayed.
	 */
	private boolean												metadataChangedForDisplay;

	private Map<String, String>						cachedNaturalIdValues;
	
	private boolean												recycled;

	/**
	 * a map from <link_with> type to linked Metadata. initial empty. no key indicates not yet tried
	 * download and parse. having key but null value indicates tried but failed.
	 */
	private Map<String, Metadata>					linkedMetadata;

	private Object												lockLinkedMetadata				= new Object();
	
	/**
	 * a list of linked metadata, which is used for de/serialization. the map (linkedMetadata) is not
	 * used for de/serialization because thek key really should not be meta-metadata type. instead,
	 * this field serves as a surrogate for de/serialization. at runtime, whenever the map is updated
	 * this field is updated accordingly. also, linkedMetadata will be initialized using this field
	 * in lazy evaluation.
	 */
	@mm_name("linked_metadata_list")
	@simpl_collection("linked_metadata")
	private ArrayList<Metadata>						linkedMetadataList;

	/**
	 * This constructor should *only* be used when marshalled Metadata is read.
	 */
	public Metadata()
	{
		super();
	}

	/**
	 * This constructor should be used by *all* live code.
	 * 
	 * @param metaMetadata
	 */
	public Metadata(MetaMetadataCompositeField metaMetadata)
	{
		this();
		setMetaMetadata(metaMetadata);
	}

	/**
	 * Don't exclude the mixins field from Metadata.
	 * 
	 * @param tagName
	 * 
	 * @return true for the mixins field; otherwise false;
	 */
	public boolean excludeFieldByTag(String tagName)
	{
		return MIXINS_FIELD_NAME.equals(tagName);
	}

	//
	public MetadataClassDescriptor getMetadataClassDescriptor()
	{
		return (MetadataClassDescriptor) classDescriptor();
	}

	/**
	 * If necessary, bind a MetaMetadata object to this. Save the result for next time.
	 * 
	 * @return
	 */
	public MetaMetadataCompositeField getMetaMetadata()
	{
		// return getMetadataClassDescriptor().getMetaMetadata();
		MetaMetadataCompositeField mm = metaMetadata;
		if (mm == null && repository != null)
		{
			if (metaMetadataName != null) // get from saved composition
				mm = repository.getByTagName(metaMetadataName.getValue());

			if (mm == null)
			{
				ParsedURL location = getLocation();
				if (location != null)
				{
					if (isImage())
						mm = repository.getImageMM(location);
					else
						mm = repository.getDocumentMM(location);

					// TODO -- also try to resolve by mime type ???
				}
				if (mm == null)
					mm = repository.getByClass(getClass());
				if (mm == null && classDescriptor() != null)
				{
					mm = repository.getByTagName(classDescriptor().getTagName());
				}
			}
			if (mm != null)
				setMetaMetadata(mm);
			// metaMetadata = mm;
		}
		return mm;
	}

	/**
	 * 
	 */
	ArrayList<Metadata> mixins()
	{
		ArrayList<Metadata> result = this.mixins;
		if (result == null)
		{
			result = new ArrayList<Metadata>();
			this.mixins = result;
		}
		return result;
	}

	public void addMixin(Metadata mixin)
	{
		if (mixin != null)
		{
			ArrayList<Metadata> mixins = mixins();
			if (!mixins.contains(mixin))
			{
				mixins.add(mixin);
				if (mixin.termVector() != null)
					termVector().add(mixin.termVector());
			}
		}
	}

	public void removeMixin(Metadata mixin)
	{
		if (mixin != null)
		{
			if (mixins().remove(mixin) && mixin.termVector != null)
				termVector().remove(mixin.termVector);
		}
	}

	/**
	 * @return the number of non-Null fields within this metadata
	 */
	public int size()
	{
		return numberOfVisibleFields();
	}
	
	public int numberOfVisibleFields()
	{
		return numberOfVisibleFields(true);
	}

	public int numberOfVisibleFields(boolean considerAlwaysShow)
	{
		int size = 0;

		MetaMetadataOneLevelNestingIterator fullIterator = fullNonRecursiveMetaMetadataIterator(null);
		// iterate over all fields in this & then in each mixin of this
		while (fullIterator.hasNext())
		{
			MetaMetadataField metaMetadataField = fullIterator.next();
			MetaMetadataField metaMetadata = fullIterator.currentObject(); // stays the same for until we
																																			// iterate over all mfd's for
																																			// it
			Metadata currentMetadata = fullIterator.currentMetadata();

			// When the iterator enters the metadata in the mixins "this" in getValueString has to be
			// the corresponding metadata in mixin.
			boolean hasVisibleNonNullField = false;
			MetadataFieldDescriptor mfd = metaMetadataField.getMetadataFieldDescriptor();

			if (metaMetadata.isChildFieldDisplayed(metaMetadataField.getName()))
			{
				if (mfd.isScalar())
					hasVisibleNonNullField = MetadataString.isNotNullAndEmptyValue(mfd
							.getValueString(currentMetadata));
				else if (mfd.isNested())
				{
					Metadata nestedMetadata = (Metadata) mfd.getNested((ElementState) currentMetadata);
					hasVisibleNonNullField = (nestedMetadata != null) ? (nestedMetadata
							.numberOfVisibleFields() > 0) : false;
				}
				else if (mfd.isCollection())
				{
					Collection collection = mfd.getCollection(currentMetadata);
					hasVisibleNonNullField = (collection != null) ? (collection.size() > 0) : false;
				}
			}

			// "null" happens with mixins fieldAccessor b'coz getValueString() returns "null".

			// TODO use MetaMetadataField.numNonDisplayedFields()
			boolean isVisibleField = !metaMetadataField.isHide()
					&& ((considerAlwaysShow && metaMetadataField.isAlwaysShow()) || hasVisibleNonNullField);

			if (isVisibleField)
				size++;
		}

		return size;
	}

	public void rebuildTotally()
	{
		if (termVector != null)
			termVector.reinitialize();
		rebuildCompositeTermVector();
	}

	/**
	 * Rebuilds the composite TermVector from the individual TermVectors FIXME:Not able to move to the
	 * MetadataBase b'coz of mixins.
	 */
	@Override
	public void rebuildCompositeTermVector()
	{
		// if there are no metadatafields retain the composite termvector
		// because it might have meaningful entries
		if (termVector == null)
		{
			initializeMetadataCompTermVector();
			return;
		}

		Set<ITermVector> vectors = termVector.componentVectors();

		ClassAndCollectionIterator i = metadataIterator();
		while (i.hasNext())
		{
			MetadataBase mb = i.next();
			if (mb != null)
			{
				// if mb is a Metadata object, this call may recursively initialize its CompositeTermVector
				ITermVector mTermVector = mb.termVector();
				if (mb != null && !vectors.contains(mTermVector))
					termVector.add(mTermVector);
			}
		}
	}

	@Override
	protected void serializationPreHook()
	{
		getMetaMetadata();
	}

	@Override
	protected void deserializationPostHook()
	{
		// if (metaMetadata != null)
		// initializeMetadataCompTermVector();
	}

	public boolean hwSet(String tagName, String value)
	{
		if (setByTagName(tagName, value))
		{
			// value is properly set.
			rebuildCompositeTermVector();
			return true;
		}
		return false;
	}

	public Field getFields()
	{

		return null;
	}

	public void setMetaMetadata(MetaMetadataCompositeField metaMetadata)
	{
		// FIXME -- get rid of all call sites for this method -- andruid 6/1/10
		// see MetaMetadataSearchParser for a call site. can we avoid this call?

		this.metaMetadata = metaMetadata;
		String metaMetadataName = metaMetadata.getName();
		if (!classDescriptor().getTagName().equals(metaMetadataName)) // avoid writing these when you
																																	// don't need them
			this.metaMetadataName = new MetadataString(metaMetadataName);
	}

	@Override
	public CompositeTermVector termVector()
	{
		if (termVector == null && metaMetadata != null)
			return initializeMetadataCompTermVector();
		return termVector;
	}

	// could get called twice if termVector() is called from different threads & termVector is null.
	public synchronized CompositeTermVector initializeMetadataCompTermVector()
	{
		if (termVector != null)
			return termVector;
		CompositeTermVector tv = new CompositeTermVector();
		ClassAndCollectionIterator i = metadataIterator();
		while (i.hasNext())
		{
			MetadataBase mb = i.next();

			MetaMetadataField currentMMField = i.getCurrentMMField();
			if (mb != null && !currentMMField.isIgnoreInTermVector()
					&& !mb.ignoreInTermVector())
			{
				tv.add(mb.termVector());
			}
		}
		return (termVector = tv);
	}
	
	/**
	 * In general, Metadata objects should contribute to the CompositeTermVector.
	 */
	@Override
	public boolean ignoreInTermVector()
	{
		return false;
	}


	public ParsedURL getLocation()
	{
		return null;
	}

	public void hwSetLocation(ParsedURL location)
	{
	}

	public void setLocation(ParsedURL location)
	{
	}

	public String getContext()
	{
		return null;
	}

	/**
	 * Sets the value of the field context
	 **/

	public void setContext(String context)
	{
	}

	public void hwSetContext(String context)
	{
	}

	public ParsedURL getNavLocation()
	{
		return null;
	}

	public void setNavLocation(ParsedURL navLocation)
	{
	}

	public void hwSetNavLocation(ParsedURL navLocation)
	{
	}

	/**
	 * @return the mixins
	 */
	public ArrayList<Metadata> getMixins()
	{
		return mixins == null ? null : mixins();
	}

	/**
	 * @param mixins
	 *          the mixins to set
	 */
	public void setMixins(ArrayList<Metadata> mixins)
	{
		this.mixins = mixins;
	}

	// For adding mapped attributes
	public void add(String key)
	{

	}

	public Metadata get(int index)
	{
		return null;
	}

	@Override
	public void recycle()
	{
		if (recycled)
			return;
		recycled					= true;
		
		if (metaMetadata != null)
		{
			MetaMetadataRepository repository = metaMetadata.getRepository();
			if (repository != null)
			{
				LinkedMetadataMonitor monitor = repository.getLinkedMetadataMonitor();
				monitor.removeMonitors(this);
			}
		}

		ClassAndCollectionIterator iterator = metadataIterator();

		for (MetadataBase metadata = iterator.next(); iterator.hasNext(); metadata = iterator.next())
		{
			if (metadata != null)
				metadata.recycle();
		}

		super.recycle();
		if (termVector != null)
		{
			// termVector can be null for those metadata created on the fly, e.g. ImageElement created
			// from semantic action without metadata information (with only location & caption).
			termVector.recycle();
			termVector = null;
		}
	}

	public boolean isRecycled()
	{
		return recycled; // (termVector != null && termVector.isRecycled());
	}

	public MetaMetadataOneLevelNestingIterator fullNonRecursiveMetaMetadataIterator(
			MetaMetadataField metaMetadataField)
	{
		MetaMetadataField firstMetaMetadataField = (metaMetadataField != null) ? metaMetadataField
				: metaMetadata;
		return new MetaMetadataOneLevelNestingIterator(firstMetaMetadataField, this);
	}

	public ClassAndCollectionIterator metadataIterator()
	{
		return new ClassAndCollectionIterator(metaMetadata, this);
	}

	public boolean hasObservers()
	{
		return termVector != null && termVector.countObservers() > 0;
	}

	public boolean hasTermVector()
	{
		return termVector != null;
	}

	public static void setRepository(MetaMetadataRepository repo)
	{
		repository = repo;
	}

	// FIXEME:The method has to search even all the mixins for the key.
	public MetadataFieldDescriptor getFieldDescriptorByTagName(String tagName)
	{
		return getMetadataClassDescriptor().getFieldDescriptorByTag(tagName,
				repository.metadataTranslationScope());
	}

	/**
	 * Sets the field to the specified value and wont rebuild composteTermVector
	 * 
	 * @param fieldName
	 * @param value
	 */
	// TODO -- May throw exception if there is no field accessor.
	// FIXME -- resolve with MetadataBase
	// public boolean setByTagName(String tagName, String value)
	// {
	// tagName = tagName.toLowerCase();
	// // Taking care of mixins
	// Metadata metadata = getMetadataWhichContainsField(tagName);
	//
	// if (value != null && value.length() != 0)
	// {
	// if (metadata != null)
	// {
	// FieldDescriptor fieldAccessor = getFieldDescriptorByTagName(tagName);
	// if (fieldAccessor != null && value != null && value.length() != 0)
	// {
	// fieldAccessor.set(metadata, value);
	// return true;
	// }
	// else
	// {
	// debug("Not Able to set the field: " + tagName);
	// return false;
	// }
	// }
	// }
	// return false;
	// }

	public boolean setByTagName(String tagName, String value)
	{
		return setByTagName(tagName, value, null);
	}

	/**
	 * Unmarshall the valueString and set the field to
	 * 
	 * @param tagName
	 * @param marshalledValue
	 * @param scalarUnMarshallingContext
	 * @return
	 */
	public boolean setByTagName(String tagName, String marshalledValue,
			ScalarUnmarshallingContext scalarUnMarshallingContext)
	{
		// FIXME -- why is this necessary???????????????????????
		if (marshalledValue != null && marshalledValue.length() != 0)
		{
			tagName = tagName.toLowerCase(); // FIXME -- get rid of this!
			MetadataFieldDescriptor fieldDescriptor = getFieldDescriptorByTagName(tagName);
			if (fieldDescriptor != null /* && value != null && value.length()!=0 */) // allow set to
																																								// nothing --
																																								// andruid & andrew
																																								// 4/14/09
			{
				// FIXME -- override this method in MetadataFieldDescriptor!!!
				fieldDescriptor.set(this, marshalledValue, scalarUnMarshallingContext);
				return true;
			}
			else
			{
				debug("Not Able to set the field: " + tagName);
			}
		}
		return false;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@Inherited
	public @interface semantics_mixin
	{

	}

	@Override
	public boolean hasCompositeTermVector()
	{
		return termVector != null;
	}

	public Iterator<MetadataFieldDescriptor> iterator()
	{
		return classDescriptor().iterator();
	}

	/**
	 * @return
	 */
	public HashMapArrayList<String, MetadataFieldDescriptor> getFieldDescriptorsByFieldName()
	{
		return classDescriptor().getFieldDescriptorsByFieldName();
	}

	/**
	 * Convenience method for type checking related to Image-ness.
	 * Base implementation:
	 * 
	 * @return	false
	 */
	public boolean isImage()
	{
		return false;
	}

	/**
	 * Convenience method for type checking related to CompoundDocument-ness.
	 * Base implementation:
	 * 
	 * @return	false
	 */
	public boolean isCompoundDocument()
	{
		return false;
	}

	public boolean isGui()
	{
		return false;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Target(ElementType.FIELD)
	public @interface mm_name
	{
		String value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Target(ElementType.FIELD)
	public @interface mm_dont_inherit
	{
	}

	public void serializeToHtml(Appendable a, TranslationContext serializationContext)
			throws IllegalArgumentException, IllegalAccessException, IOException, SIMPLTranslationException
	{
		Table htmlTable = new Table();
		renderHtml(htmlTable, serializationContext, false, true);
		htmlTable.serialize(a);
	}

	public void renderHtml(Table htmlTable, TranslationContext serializationContext, boolean recursing,
			boolean encapsulateInTable) throws IllegalArgumentException,
			IllegalAccessException, IOException
	{

		MetadataClassDescriptor classDescriptor = this.getMetadataClassDescriptor();
		MetaMetadataOneLevelNestingIterator fullIterator = fullNonRecursiveMetaMetadataIterator(null);

		int numElements = numberOfVisibleFields(false);
		boolean hasXmlText = classDescriptor.hasScalarFD();

		if (numElements > 0 || hasXmlText)
		{
			Tr tr = new Tr();

			Table compositeTable	= new Table();
			Td nestedTd 					= new Td();

			if (recursing && numElements > 1)
			{
				

				Td buttonTd = new Td();
				Div div = new Div();
				Input button = new Input();
				
				div.setCssClass("metadata_text");
				htmlTable.setCssClass("nested_table");
				buttonTd.setCssClass("metadata_field_name");
				button.setType("image");
				button.setCssClass("composite");
				button.setSrc("http://ecologylab.net/cf/compositionIncludes/button.jpg");
				button.setValue("");

				div.members.add(button);
				div.setText(metaMetadata.getDisplayedLabel());
				buttonTd.items.add(div);
				tr.cells.add(buttonTd);
				htmlTable.rows.add(tr);
			}
			while (fullIterator.hasNext())
			{
				MetaMetadataField mmdField = fullIterator.next();
				final Metadata currentMetadata = fullIterator.currentMetadata();
				MetadataFieldDescriptor childFD = mmdField.getMetadataFieldDescriptor();
				FieldDescriptor navigatesFD = this.getFieldDescriptorByTagName(mmdField.getNavigatesTo());
				
				if (!mmdField.isHide())
				{
					final int type = childFD.getType();
					if (type == SCALAR)
					{
						if (!childFD.getScalarType().isDefaultValue(childFD.getField(), currentMetadata))
						{

							Tr scalarTr = new Tr();
							
							scalarTr.setId("mmd_" + childFD.getTagName());

							childFD.appendHtmlValueAsAttribute(currentMetadata, serializationContext, mmdField.getStyle(),
									navigatesFD, mmdField.getDisplayedLabel(), scalarTr);

							if (recursing)
								compositeTable.rows.add(scalarTr);
							
							htmlTable.rows.add(scalarTr);
						}
					}
					else
					{
						Object thatReferenceObject = null;
						Field childField = childFD.getField();
						thatReferenceObject = childField.get(this);

						if (thatReferenceObject == null)
							continue;

						final boolean isScalar = (type == COLLECTION_SCALAR || type == MAP_SCALAR);
						Collection thatCollection;
						
						switch (type)
						{
						case COLLECTION_ELEMENT:
						case COLLECTION_SCALAR:
						case MAP_ELEMENT:
						case MAP_SCALAR:
							thatCollection = XMLTools.getCollection(thatReferenceObject);
							break;
						default:
							thatCollection = null;
							break;
						}

						if ((thatCollection != null) && (thatCollection.size() != 0))
						{
							int i = 0;
							Tr nestedTr = new Tr();
							
							nestedTr.setCssClass("nested");

							if (childFD.isWrapped())
								childFD.writeHtmlWrap(false, thatCollection.size(), mmdField.getDisplayedLabel(), nestedTr);
							Td collectionTd = new Td();
							collectionTd.setCssClass("nested_field_value");
							
							for (Object next : thatCollection)
							{
								ElementState nestedES = (ElementState) next;
								FieldDescriptor compositeAsScalarFD = nestedES.classDescriptor().getScalarValueFieldDescripotor();
								
								if (isScalar)
									childFD.appendHtmlValueAsAttribute(currentMetadata, serializationContext, mmdField.getStyle(), navigatesFD, null, nestedTr);
								else if (compositeAsScalarFD != null)
								{
									Span compositeAsScalarSpan = new Span();
									compositeAsScalarSpan.setCssClass("composite_as_scalar");
									collectionTd.spans.add(compositeAsScalarFD.getHtmlCompositeCollectionValue(nestedES, i == 0));
								}
								i++;
							}
							
							nestedTr.cells.add(collectionTd);
							
							for (Object next : thatCollection)
							{
								if (next instanceof Metadata)
								{
									Table nestedTable = new Table();
									Metadata collectionSubElementState = (Metadata) next;
									collectionSubElementState.renderHtml(nestedTable, serializationContext, true, true);
									collectionTd.items.add(nestedTable);
								}

							}
							if (childFD.isWrapped())
								nestedTr.cells.add(collectionTd);
							htmlTable.rows.add(nestedTr);
						}
						else if (thatReferenceObject instanceof Metadata)
						{
							Tr compositeTr = new Tr();
							Td compositeTd = new Td();
							
							tr.setCssClass("nested");
							
							Metadata nestedMD = (Metadata) thatReferenceObject;
							ElementState nestedES = (ElementState) thatReferenceObject;
							FieldDescriptor compositeAsScalarFD = nestedES.classDescriptor().getScalarValueFieldDescripotor();

							if (compositeAsScalarFD != null)
							{
								childFD.writeCompositeHtmlWrap(false, mmdField.getDisplayedLabel(), compositeTr);

								Span compositeAsScalarSpan = new Span();
								compositeAsScalarSpan.setCssClass("composite_as_scalar");
								compositeTd.spans.add(compositeAsScalarFD.getHtmlCompositeCollectionValue(nestedES, true));
							}
							else
							{
								Table nestedTable = new Table();
								nestedMD.renderHtml(nestedTable, serializationContext, true, false);
								compositeTd.items.add(nestedTable);
							}
							htmlTable.rows.add(compositeTr);
						}
					}
				}
			}
			if (recursing && numElements > 1)
			{
				compositeTable.rows.add(tr);
				nestedTd.items.add(compositeTable);
				tr.cells.add(nestedTd);
			}
			htmlTable.rows.add(tr);
		}
	}

	public boolean hasMetadataChanged()
	{
		return metadataChangedForDisplay;
	}

	public void setMetadataChanged(boolean value)
	{
		this.metadataChangedForDisplay = value;
	}

	/**
	 * Iterator over fields to find all metadata fields that have changed.
	 * 
	 * @return Collection of fields that have changed since last displayed.
	 */
	public ArrayList<Metadata> findChangedMetadataFields()
	{
		ArrayList<Metadata> result = new ArrayList<Metadata>();

		ClassAndCollectionIterator iterator = this.metadataIterator();
		while (iterator.hasNext())
		{
			MetadataBase fieldValue = iterator.next();
			if (fieldValue instanceof Metadata)
			{
				final Metadata metadata = (Metadata) fieldValue;
				if (metadata.hasMetadataChanged())
					result.add(metadata);

				result.addAll(metadata.findChangedMetadataFields());
			}
		}

		return result;
	}

	/**
	 * Lookup the value of a metadata field specified by "." delimited name used to navigate the
	 * meta-metadata tree.
	 * 
	 * @param fieldPath
	 *          "." delimited path to metadata field
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public MetadataBase lookupMetadataValue(String fieldPath) throws IllegalArgumentException,
			IllegalAccessException
	{
		String[] split = fieldPath.split("\\.", 2);
		MetaMetadataField mmField = metaMetadata.lookupChild(split[0]);
		MetadataBase value = (MetadataBase) mmField.getMetadataFieldDescriptor().getField().get(this);
		if (split.length > 1)
			value = ((Metadata) value).lookupMetadataValue(split[1]);

		return value;
	}

	public String getNaturalIdValue(String naturalId)
	{
		if (cachedNaturalIdValues == null)
		{
			cachedNaturalIdValues = new HashMap<String, String>();
		}
		if (cachedNaturalIdValues.containsKey(naturalId))
			return cachedNaturalIdValues.get(naturalId);
		else
		{
			String value = getNaturalIdValueHelper(naturalId);
			cachedNaturalIdValues.put(naturalId, value);
			return value;
		}
	}

	private String getNaturalIdValueHelper(String naturalId)
	{
		MetaMetadataCompositeField mmcf = (MetaMetadataCompositeField) getMetaMetadata();
		if (mmcf == null)
			return null;

		MetaMetadata mmd;
		if (mmcf instanceof MetaMetadata)
		{
			mmd = (MetaMetadata) mmcf;
		}
		else
		{
			String mmdName = mmcf.getTypeName();
			mmd = repository.getByTagName(mmdName);
		}
		MetaMetadataField field = mmd.getNaturalIdFields().get(naturalId);
		MetadataFieldDescriptor fd = field.getMetadataFieldDescriptor();
		String valueString = fd.getValueString(this);
		String format = field.getFormat();
		if (format != null)
		{
			// built-in support for text format
			if ("text".equals(format))
			{
				valueString = valueString.trim().replaceAll("[^a-zA-Z0-9]", " ").replaceAll("\\s+", " ").toLowerCase();
			}
		}
		
		return valueString;
	}
	
	private Map<String, Metadata> getLinkedMetadata()
	{
		if (linkedMetadata == null)
		{
			synchronized (lockLinkedMetadata)
			{
				if (linkedMetadata == null)
				{
					Map<String, Metadata> linkedMetadata = new HashMap<String, Metadata>();
					if (linkedMetadataList != null)
					{
						for (Metadata linkedMd : linkedMetadataList)
						{
							linkedMetadata.put(linkedMd.getMetaMetadata().getName(), linkedMd);
						}
					}
					this.linkedMetadata = linkedMetadata;
				}
			}
		}
		return linkedMetadata;
	}

	public Set<String> getLinkedMetadataKeys()
	{
		synchronized (lockLinkedMetadata)
		{
			return getLinkedMetadata().keySet();
		}
	}

	public Metadata getLinkedMetadata(String name)
	{
		synchronized (lockLinkedMetadata)
		{
			return getLinkedMetadata().get(name);
		}
	}

	public void addLinkedMetadata(LinkWith lw, Metadata metadata)
	{
		synchronized (lockLinkedMetadata)
		{
			getLinkedMetadata().put(lw.key(), metadata);
			if (linkedMetadataList == null)
				linkedMetadataList = new ArrayList<Metadata>();
			linkedMetadataList.add(metadata);
		}
	}

	public SemanticActionHandler	pendingSemanticActionHandler;

}
