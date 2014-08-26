package ecologylab.bigsemantics.metadata;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ecologylab.bigsemantics.actions.SemanticActionHandler;
import ecologylab.bigsemantics.collecting.LinkedMetadataMonitor;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
import ecologylab.bigsemantics.metadata.builtins.declarations.MetadataDeclaration;
import ecologylab.bigsemantics.metadata.output.MetadataConstants;
import ecologylab.bigsemantics.metadata.scalar.MetadataString;
import ecologylab.bigsemantics.metametadata.ClassAndCollectionIterator;
import ecologylab.bigsemantics.metametadata.LinkWith;
import ecologylab.bigsemantics.metametadata.MetaMetadata;
import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
import ecologylab.bigsemantics.metametadata.MetaMetadataField;
import ecologylab.bigsemantics.metametadata.MetaMetadataNestedField;
import ecologylab.bigsemantics.metametadata.MetaMetadataOneLevelNestingIterator;
import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
import ecologylab.bigsemantics.metametadata.declarations.MetaMetadataFieldDeclaration;
import ecologylab.bigsemantics.model.text.CompositeTermVector;
import ecologylab.bigsemantics.model.text.ITermVector;
import ecologylab.bigsemantics.model.text.OrderedNormalizedTermVectorCache;
import ecologylab.bigsemantics.model.text.TermVectorFeature;
import ecologylab.generic.Debug;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.FieldDescriptor;
import ecologylab.serialization.FieldType;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.deserializers.ISimplDeserializationPost;
import ecologylab.serialization.formatenums.StringFormat;
import ecologylab.serialization.library.html.Div;
import ecologylab.serialization.library.html.Input;
import ecologylab.serialization.library.html.Table;
import ecologylab.serialization.library.html.Td;
import ecologylab.serialization.library.html.Tr;
import ecologylab.serialization.serializers.ISimplSerializationPre;

/**
 * This is the new metadata class that is the base class for the meta-metadata system. It contains
 * all the functionality of the previous Metadata, and MetadataField classes.
 * 
 * Classes will extend this base class to provide a nested metadata structure.
 * 
 * @author sashikanth
 * 
 */
public abstract class Metadata extends MetadataDeclaration
implements MetadataBase, TermVectorFeature, Iterable<MetadataFieldDescriptor>,
ISimplSerializationPre, ISimplDeserializationPost
{
	
	final static int											INITIAL_SIZE							= 5;

	private static final String						MIXINS_FIELD_NAME					= "mixins";
	
	/**
	 * Hidden reference to the MetaMetadataRepository. DO NOT access this field directly. DO NOT
	 * create a static public accessor. -- andruid 10/7/09.
	 */
	private static MetaMetadataRepository	repository;
	
	/**
	 * this must be a composite field. this is not the meta-metadata representing type, but the
	 * "local" composite field object that may carry extraction / presentation rules.
	 */
	private MetaMetadata									metaMetadata;
	
	/**
	 * this is used by the ORM module as a surrogate identifier. its value will be generated by the
	 * database automatically. each metadata object will have a unique one for reference. using
	 * surrogate ID prevents potential conflicts problems, when used with strict relational database
	 * systems.
	 */
	private long													ormId;
	
	@simpl_scalar
	private float                         repositoryVersion;

	/**
	 * the (composite) term vector for this field.
	 */
	protected CompositeTermVector					termVector								= null;

	public static String USE_SEMANTIC_SEARCH_PREF = "use_situated_search";
	/**
	 * the cache for ordered normalized term vectors
	 */
	protected OrderedNormalizedTermVectorCache orderedCompositeTermVectorCache = null;
	
	/**
	 * Indicates whether or not metadata has changed since last displayed.
	 */
	private boolean												metadataChangedForDisplay;

	/**
	 * caching natural ID name-value pairs.
	 */
	private Map<String, String>						cachedNaturalIdValues;
	
	/**
	 * if this has been recycled.
	 */
	private boolean												recycled;

	/**
	 * a map from <link_with> type to linked Metadata. initial empty. no key indicates not yet tried
	 * download and parse. having key but null value indicates tried but failed.
	 */
	private Map<String, Metadata>					linkedMetadata;

	/**
	 * used for synchronization.
	 */
	private Object												lockLinkedMetadata				= new Object();
	
	private MetadataClassDescriptor				classDescriptor;
	
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
		if (metaMetadata != null)
			setMetaMetadata(metaMetadata);
	}

	/**
	 * get the ormId.
	 * 
	 * @return
	 */
	public long getOrmId()
	{
		return ormId;
	}
	
	/**
	 * set the ormId.
	 * 
	 * @param ormId
	 */
	public void setOrmId(long ormId)
	{
		this.ormId = ormId;
	}

	public float getRepositoryVersion()
  {
    return repositoryVersion;
  }

  public void setRepositoryVersion(float repositoryVersion)
  {
    this.repositoryVersion = repositoryVersion;
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

	public MetadataClassDescriptor getMetadataClassDescriptor()
	{
		MetadataClassDescriptor result = this.classDescriptor;
		if (result == null)
		{
			result = (MetadataClassDescriptor) ClassDescriptor.getClassDescriptor(this);
			this.classDescriptor = result;
		}
		return result;
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
			MetadataString metaMetadataName = getMetaMetadataNameMetadata();
			MetaMetadataCompositeField bySavedName = metaMetadataName == null ? null : repository.getMMByName(metaMetadataName.getValue());
			if (bySavedName != null)
				mm = bySavedName;
			else
			{
				ParsedURL location = getLocation();
				MetaMetadataCompositeField byLocation = location == null ? null : isImage() ? repository.getImageMM(location) : repository.getDocumentMM(location);
				
				// TODO by MIME type
				
				MetaMetadataCompositeField byClass = repository.getMMByClass(getClass());
				
				// I believe that in all the cases, we should use the class instead of the tag name, since
				// the same tag name can be used for different classes! -- yin qu
//				ClassDescriptor cd = classDescriptor();
//				MetaMetadataCompositeField byTagName = cd == null ? null : repository.getMMByName(cd.getTagName());
				
				// in most cases, we might use the one by location / mime type;
				// but if the one by class / tag name is more specific, we might want that one (?)
				if (byLocation != null)
				{
					if (byClass != null && byLocation.getMetadataClass().isAssignableFrom(byClass.getMetadataClass()))
						mm = byClass;
					else if (byClass != null && byLocation.isGenericMetadata() && !byClass.isGenericMetadata())
						mm = byClass;
//					else if (byTagName != null && byLocation.getMetadataClass().isAssignableFrom(byTagName.getMetadataClass()))
//						mm = byTagName;
					else
						mm = byLocation;
				}
				else
					mm = byClass;
			}
			
			if (mm != null)
				setMetaMetadata(mm);
		}
		return mm;
	}

	/**
	 * get mixins.
	 * 
	 * @return
	 */
	public List<Metadata> mixins()
	{
		List<Metadata> result = this.getMixins();
		if (result == null)
		{
			result = new ArrayList<Metadata>();
			this.setMixins(result);
		}
		return result;
	}

	public void addMixin(Metadata mixin)
	{
		if (mixin != null)
		{
			List<Metadata> mixins = mixins();
			if (!mixins.contains(mixin))
			{
				HashSet<Metadata> visitedMetadata = new HashSet<Metadata>();
				mixins.add(mixin);
				CompositeTermVector mixinTermVector = mixin.termVector(visitedMetadata);
				if (mixinTermVector != null)
					termVector().add(mixinTermVector);
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

//			if (metaMetadata.isChildFieldDisplayed(metaMetadataField.getName()))
//			{
//				if (mfd.isScalar() && !mfd.isCollection())
//					hasVisibleNonNullField = MetadataString.isNotNullAndEmptyValue(mfd
//							.getValueString(currentMetadata));
//				else if (mfd.isNested())
//				{
//					Metadata nestedMetadata = (Metadata) mfd.getNestedMetadata(currentMetadata);
//					hasVisibleNonNullField = (nestedMetadata != null) ? (nestedMetadata
//							.numberOfVisibleFields() > 0) : false;
//				}
//				else if (mfd.isCollection())
//				{
//					Collection collection = mfd.getCollection(currentMetadata);
//					hasVisibleNonNullField = (collection != null) ? (collection.size() > 0) : false;
//				}
//			}

			// "null" happens with mixins fieldAccessor b'coz getValueString() returns "null".

			// TODO use MetaMetadataField.numNonDisplayedFields()
//			boolean isVisibleField = !metaMetadataField.isHide()
//					&& ((considerAlwaysShow && metaMetadataField.isAlwaysShow()) || hasVisibleNonNullField);

			if (hasVisibleNonNullField)
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
		HashSet<Metadata> visitedMetadata = new HashSet<Metadata>();
		// if there are no metadatafields retain the composite termvector
		// because it might have meaningful entries
		if (termVector == null)
		{
			initializeMetadataCompTermVector(visitedMetadata);
			return;
		}

		Set<ITermVector> vectors = termVector.componentVectors();

		ClassAndCollectionIterator i = metadataIterator(visitedMetadata);
		while (i.hasNext())
		{
			MetadataBase mb = i.next();
			if (mb != null)
			{
				// if mb is a Metadata object, this call may recursively initialize its CompositeTermVector
				ITermVector mTermVector = mb.termVector(visitedMetadata);
				if (mb != null && !vectors.contains(mTermVector))
					termVector.add(mTermVector);
			}
		}
	}
	
	public void rebuildCompositeTermVector(HashSet<Metadata> visitedMetadata)
	{
		// if there are no metadatafields retain the composite termvector
		// because it might have meaningful entries
		if (termVector == null)
		{
			initializeMetadataCompTermVector(visitedMetadata);
			return;
		}

		Set<ITermVector> vectors = termVector.componentVectors();

		ClassAndCollectionIterator i = metadataIterator(visitedMetadata);
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
	public void serializationPreHook(TranslationContext translationContext)
	{
		getMetaMetadata();
	}

	@Override
	public void deserializationPostHook(TranslationContext translationContext, Object object)
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

	public void setMetaMetadata(MetaMetadataNestedField metaMetadata)
	{
		// FIXME -- get rid of all call sites for this method -- andruid 6/1/10
		// see MetaMetadataSearchParser for a call site. can we avoid this call?

		if (metaMetadata instanceof MetaMetadata)
			this.metaMetadata = (MetaMetadata) metaMetadata;
		else
			this.metaMetadata = metaMetadata.getTypeMmd();
		
		String metaMetadataName = this.metaMetadata.getName();
		this.setMetaMetadataNameMetadata(new MetadataString(metaMetadataName));
	}

	@Override
	public CompositeTermVector termVector()
	{
		return getOrCreateTermVector();
	}
	
	@Override
	public CompositeTermVector termVector(HashSet<Metadata> visitedMetadata)
	{
		if (termVector == null && metaMetadata != null)
			return initializeMetadataCompTermVector(visitedMetadata);
		return termVector;
	}

	// could get called twice if termVector() is called from different threads & termVector is null.
	public synchronized CompositeTermVector initializeMetadataCompTermVector(HashSet<Metadata> visitedMetadata)
	{
		CompositeTermVector tv = getOrCreateTermVector();
		ClassAndCollectionIterator i = metadataIterator(visitedMetadata);
		while (i.hasNext())
		{
			MetadataBase mb = i.next();

			MetaMetadataField currentMMField = i.getCurrentMMField();
			if (mb != null && !currentMMField.isIgnoreInTermVector()
					&& !mb.ignoreInTermVector())
			{
				tv.add(mb.termVector(visitedMetadata));
			}
		}
		return (termVector = tv);
	}
	
	public void clearOrderedNormalizedTermVectorCache()
	{
		orderedCompositeTermVectorCache = null;
	}
	
	public OrderedNormalizedTermVectorCache getOrCreateOrderedNormalizedTermVectorCache()
	{
		if(orderedCompositeTermVectorCache == null)
		{
			try
			{
			   orderedCompositeTermVectorCache = new OrderedNormalizedTermVectorCache(termVector());
			}
			catch(Throwable t)
			{
				return new OrderedNormalizedTermVectorCache();
			}
		}
		return orderedCompositeTermVectorCache;
	}

	protected CompositeTermVector getOrCreateTermVector()
	{
		if (termVector != null)
			return termVector;
		CompositeTermVector tv = new CompositeTermVector();
		return tv;
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
		// TODO recycling
		this.recycled = true;
	}

	public void recycle(HashSet<Metadata> visitedMetadata)
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

		ClassAndCollectionIterator iterator = metadataIterator(visitedMetadata);

		for (MetadataBase metadata = iterator.next(); iterator.hasNext(); metadata = iterator.next())
		{
			if (metadata != null)
				metadata.recycle();
		}

		this.recycle();
		
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
	
	public void resetRecycleStatus()
	{
	  recycled = false;
	}

	public MetaMetadataOneLevelNestingIterator fullNonRecursiveMetaMetadataIterator(
			MetaMetadataField metaMetadataField)
	{
		MetaMetadataField firstMetaMetadataField =
		    (metaMetadataField != null) ? metaMetadataField : metaMetadata;
		return new MetaMetadataOneLevelNestingIterator(firstMetaMetadataField, this);
	}

	public ClassAndCollectionIterator metadataIterator(HashSet<Metadata> visitedMetadata)
	{
		return new ClassAndCollectionIterator(getMetaMetadata(), this, visitedMetadata);
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
//			tagName = tagName.toLowerCase(); // FIXME -- get rid of this!
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
				Debug.debugT(this, "Not Able to set the field: " + tagName);
			}
		}
		return false;
	}
	
	public boolean setByFieldName(String fieldName, String marshalledValue)
	{
		return setByFieldName(fieldName, marshalledValue, null);
	}
	
	public boolean setByFieldName(String fieldName, String marshalledValue, ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		if (marshalledValue != null && marshalledValue.length() != 0)
		{
			MetadataFieldDescriptor fieldDescriptor = getFieldDescriptorsByFieldName().get(fieldName);
			if (fieldDescriptor != null)
			{
				fieldDescriptor.set(this, marshalledValue, scalarUnmarshallingContext);
				return true;
			}
			else
			{
				Debug.warning(this, "Cannot find field [" + fieldName + "] on " + this);
			}
		}
		return false;
	}

	@Override
	public boolean hasCompositeTermVector()
	{
		return termVector != null;
	}

	@Override
	public Iterator<MetadataFieldDescriptor> iterator()
	{
		return getMetadataClassDescriptor().iterator();
	}

	/**
	 * @return
	 */
	public HashMapArrayList<String, MetadataFieldDescriptor> getFieldDescriptorsByFieldName()
	{
		return getMetadataClassDescriptor().getFieldDescriptorsByFieldName();
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
	 * Convenience method for type checking related to InfoComposition-ness
	 * Base implementation
	 * @return false
	 */
	public boolean isInformationComposition()
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

	public void serializeToHtml(Appendable a, TranslationContext serializationContext)
			throws IllegalArgumentException, IllegalAccessException, IOException, SIMPLTranslationException
	{
		Table htmlTable = new Table();
		renderHtml(htmlTable, serializationContext, false, true, false);
		SimplTypesScope.serialize(htmlTable, a, StringFormat.XML);
		
	}
	
	
	public Table addLabelAndReturnTable(Tr outputRow, int numElements, Metadata metadata, MetaMetadataCompositeField mmCompositeField)
	{
		//Tr tr = new Tr();
		Td labelTd = new Td();
		Div div = new Div();
		Input button = new Input(); //expanding button
		
		div.setCssClass(MetadataConstants.METADATA_TEXT);//css for all
		
		///div.setText(metadata.getMetaMetadata().getDisplayedLabel());//label for all
		div.setText(mmCompositeField.getLabel());
		
		if (numElements > 1)//add button
		{
			labelTd.setCssClass(MetadataConstants.FIELD_NAME);
			button.setType(MetadataConstants.IMAGE);
			button.setCssClass(MetadataConstants.COMPOSITE);
			button.setSrc(MetadataConstants.IMAGE_URL);
			button.setValue("");
			
			div.members.add(button);
		}
		labelTd.items.add(div);
		outputRow.cells.add(labelTd);
		Table placeHolderTable = new Table();
		return placeHolderTable;
	}
	

	public void renderHtml(Table htmlTable, TranslationContext serializationContext, boolean recursing,
			boolean encapsulateInTable, boolean hideNextCompositLabel) throws IllegalArgumentException,
			IllegalAccessException, IOException, SIMPLTranslationException
	{
		renderHtml(htmlTable, serializationContext, recursing, encapsulateInTable,
				hideNextCompositLabel, new HashSet());
	}

	public void renderHtml(Table htmlTable, TranslationContext serializationContext, boolean recursing,
			boolean encapsulateInTable, boolean hideNextCompositLabel, Set bookKeeper) throws IllegalArgumentException,
			IllegalAccessException, IOException, SIMPLTranslationException
	{
		// TODO currently, when there is a back reference, we render nothing to the HTML
		// but what we actually need is to render a title or part of the metadata with a hyperlink to
		// the real data.
		if (bookKeeper.contains(this))
			return;
		bookKeeper.add(this);
		
		//System.out.println("   debug report 1: metadata name form top is "+this.getMetaMetadataName());

		MetadataClassDescriptor classDescriptor = this.getMetadataClassDescriptor();
		MetaMetadataOneLevelNestingIterator fullIterator = fullNonRecursiveMetaMetadataIterator(null);

		int numElements = numberOfVisibleFields(false);
		boolean hasXmlText = classDescriptor.hasScalarFD();

		if (numElements > 0 || hasXmlText)
		{
			Tr tr = new Tr();
			Table compositeTable	= new Table();
			Td nestedTd 					= new Td();
			
			String schemaOrgItemtype = null;
			MetaMetadataCompositeField compositeMmd = this.getMetaMetadata();
			if (compositeMmd instanceof MetaMetadata)
				schemaOrgItemtype = compositeMmd.getSchemaOrgItemtype();
			else
				schemaOrgItemtype = compositeMmd.getTypeMmd().getSchemaOrgItemtype();
			if (schemaOrgItemtype != null)
				htmlTable.setSchemaOrgItemType(schemaOrgItemtype);
			
			while (fullIterator.hasNext())
			{
				MetaMetadataField mmdField = fullIterator.next();
				final Metadata currentMetadata = fullIterator.currentMetadata();
				MetadataFieldDescriptor childFD = mmdField.getMetadataFieldDescriptor();
				FieldDescriptor navigatesFD = this.getFieldDescriptorByTagName(mmdField.getNavigatesTo());
				if (!mmdField.isHide())
				{
					FieldType type = childFD.getType();
					String textCssClass = mmdField.getStyleName();
					if (MetadataConstants.DEFAULT.equals(textCssClass))
							textCssClass		= MetadataConstants.METADATA_TEXT;
					if (type == FieldType.SCALAR)
					{
						if (!childFD.getScalarType().isDefaultValue(childFD.getField(), currentMetadata))
						{
							Tr scalarTr = new Tr();
							
							scalarTr.setId("mmd_" + childFD.getTagName());
							
							String tagName					= childFD.getTagName();
							boolean hasNavigatesTo	= navigatesFD != null;
							hasNavigatesTo 					= hasNavigatesTo && !navigatesFD.isDefaultValue(currentMetadata);
							if (!hasNavigatesTo && (tagName.equals(MetadataConstants.LOCATION) || tagName.equals(MetadataConstants.LINK)))
							{
								navigatesFD						= childFD;
							}
							childFD.appendHtmlValueAsAttribute(currentMetadata, serializationContext, scalarTr,
									mmdField.getLabel(), MetadataConstants.FIELD_NAME, textCssClass, navigatesFD, mmdField.getSchemaOrgItemprop());

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

					//	final boolean isScalar = (type == COLLECTION_SCALAR || type == MAP_SCALAR);
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
							
							nestedTr.setCssClass(MetadataConstants.NESTED);

							if (childFD.isWrapped())
								childFD.writeHtmlWrap(false, thatCollection.size(), mmdField.getLabel(), nestedTr);
							Td collectionTd = new Td();
							collectionTd.setCssClass(MetadataConstants.NESTED_VALUE);
														
							nestedTr.cells.add(collectionTd);
							
							for (Object next : thatCollection)
							{
								if (next instanceof Metadata)
								{
									//System.out.println("   debug report 4: within itterator 1 "+((Metadata)next).getMetaMetadataName());

									
									Table nestedTable = new Table();
									Metadata collectionSubElementState = (Metadata) next;
																	
									collectionSubElementState.renderHtml(nestedTable, serializationContext, true, true, false, bookKeeper); // This collection may add a composite element....
									
									//remove last row of this table because it is empty
									if(nestedTable.rows.size() > 1)
									{
										nestedTable.rows.remove(nestedTable.rows.size() - 1);
									}
									collectionTd.items.add(nestedTable);
								}
							}
							if (childFD.isWrapped())
								nestedTr.cells.add(collectionTd);
							htmlTable.rows.add(nestedTr);
						}
						else if (thatReferenceObject instanceof Metadata)	// type is COMPOSITE_ELEMENT
						{
							Tr compositeTr = new Tr();
							Td compositeTd = new Td();
							
							tr.setCssClass(MetadataConstants.NESTED);
							
							Metadata nestedMD = (Metadata) thatReferenceObject;
							FieldDescriptor compositeAsScalarFD = ClassDescriptor.getClassDescriptor(thatReferenceObject).getScalarValueFieldDescripotor();

							if (compositeAsScalarFD != null)
							{
								childFD.writeCompositeHtmlWrap(false, mmdField.getLabel(), mmdField.getSchemaOrgItemtype(), compositeTr);
							}
							else
							{
								int fieldsShownInHtml = ((Metadata)thatReferenceObject).numberOfVisibleFields(false);
								if(((Metadata)thatReferenceObject).hasLocation())
									fieldsShownInHtml -= 1;//not shown in HTML
								Table nestedTable = addLabelAndReturnTable(compositeTr, fieldsShownInHtml, (Metadata)thatReferenceObject, (MetaMetadataCompositeField) mmdField);
								nestedMD.renderHtml(nestedTable, serializationContext, true, false, true, bookKeeper);  //also called with a scholarly article...
								compositeTd.items.add(nestedTable);								
							}							
							
							if(compositeTd.items.size() > 0 && compositeTd.items.get(0) instanceof Table)
							{
								Table innerTable = (Table) compositeTd.items.get(0);
								//remove last row from this table because it is empty
								if(innerTable.rows.size() > 1)
									innerTable.rows.remove(innerTable.rows.size()-1);
							}
							
							compositeTr.cells.add(compositeTd);
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
		Debug.println(this, "setMetadataChanged()");
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

		ClassAndCollectionIterator iterator = this.metadataIterator(null);
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
		MetaMetadataCompositeField mmcf = getMetaMetadata();
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
			mmd = repository.getMMByName(mmdName);
		}
		MetaMetadataField field = mmd.getNaturalIdField(naturalId);
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
					if (getLinkedMetadataList() != null)
					{
						for (Metadata linkedMd : getLinkedMetadataList())
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
			if (getLinkedMetadataList() == null)
				setLinkedMetadataList(new ArrayList<Metadata>());
			getLinkedMetadataList().add(metadata);
		}
	}

	public SemanticActionHandler	pendingSemanticActionHandler;

	/**
	 * Determine if this already has a mixin assignable from the class passed in.
	 * 
	 * @param mixinClass
	 * @return
	 */
	public boolean containsMixin(Class<? extends Metadata> mixinClass)
	{
		if (getMixins() == null || mixinClass == null)
			return false;
		for (Metadata mixin: getMixins())
		{
			if (mixinClass.isAssignableFrom(mixin.getClass()))
				return true;
		}
		return false;
	}
	/**
	 * Determine if this already has a mixin assignable from the class passed in.
	 * 
	 * @param mixinName	The name of the meta-metadata for the mixin you seek to match.
	 * 
	 * @return
	 */
	public boolean containsMixin(String mixinName)
	{
		if (getMixins() == null || mixinName == null)
			return false;
		for (Metadata mixin: getMixins())
		{
			if (mixinName.equals(mixin.getMetaMetadata().getName()))
				return true;
		}
		return false;
	}
	
	public<MI extends Metadata> MI getMixin(Class<MI> mixinClass)
	{
		if (getMixins() == null || mixinClass == null)
			return null;
		for (Metadata mixin: getMixins())
		{
			if (mixinClass.isAssignableFrom(mixin.getClass()))
				return (MI) mixin;
		}
		return null;
	}
	
	public boolean hasLocation()
	{
		return false;
	}
	
	public boolean isClipping()
	{
		return false;
	}
	
	/**
	 * Base class method for overriding. Does nothing.
	 * @param semanticsSessionScope
	 */
	public void setSemanticsSessionScope(SemanticsGlobalScope semanticsSessionScope)
	{
	  // FIXME ??? who did this? please add comment on what's happening here
	}
	
	/**
	 * dest should be of the same type or a subtype of src.
	 * 
	 * @param dest
	 * @param src
	 */
	public static void fieldWiseCopy(Metadata dest, Metadata src)
	{
	  if (src != null && dest != null && src.getClass().isAssignableFrom(dest.getClass()))
	  {
  	  Map<String, MetadataFieldDescriptor> fields = src.getFieldDescriptorsByFieldName();
  	  for (String fieldName : fields.keySet())
  	  {
  	    MetadataFieldDescriptor fd = fields.get(fieldName);
  	    Object value = fd.getValue(src);
  	    if (value != null)
  	    {
  	      MetadataFieldDescriptor destFd = dest.getFieldDescriptorsByFieldName().get(fieldName);
  	      if (destFd != null)
  	      {
  	        destFd.setField(dest, value);
  	      }
  	    }
  	  }
  	  
  	  if (dest instanceof Document && src instanceof Document)
  	  {
  	    Document srcDoc = (Document) src;
        Document destDoc = (Document) dest;
        DocumentClosure downloadClosure = srcDoc.getOrConstructClosure();
        downloadClosure.changeDocument(destDoc);
  	  }
	  }
	}
	
}
