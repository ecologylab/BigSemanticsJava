package ecologylab.semantics.metadata;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.model.text.TermVector;
import ecologylab.model.text.WordForms;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.library.scalar.MetadataParsedURL;
import ecologylab.semantics.library.scalar.MetadataString;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.types.element.ArrayListState;

/**
 * This is the new metadata class that is the base class for the 
 * meta-metadata system. It contains all the functionality of the 
 * previous Metadata, and MetadataField classes.
 * 
 * Classes will extend this base class to provide a nested metadata structure
 * 
 * @author sashikanth
 *
 */
abstract public class Metadata extends MetadataBase
{
	MetaMetadata 							metaMetadata;
	
	/**
	 * Allows combining sinstantiated Metadata subclass declarations without hierarchy.
	 * 
	 * Could help, for example, to support user annotation.
	 */
	@xml_nested	ArrayListState<Metadata>	mixins;
	
	final static int						INITIAL_SIZE				= 5;

	/**
	 * Set to true if this cFMetadata object was restored from a saved collage.
	 * This is necessary to prevent cFMetadata from being added again and hence
	 * overwritting edited cFMetadata when the elements are recrawled on a restore.
	 */
	private boolean 						loadedFromPreviousSession 	= false;
	
	public Metadata()
	{
//		setupMetadataFieldAccessors();
	}
	
	public Metadata(MetaMetadata metaMetadata)
	{
		this.metaMetadata		= metaMetadata;
//		setupMetadataFieldAccessors();
	}
	
	/**
	 * 
	 */
	ArrayListState<Metadata> mixins()
	{
		ArrayListState<Metadata> result = this.mixins;
		if(result == null)
		{
			result = new ArrayListState<Metadata>();
			this.mixins = result;
		}
		return result;
	}
	
	public void addMixin(Metadata mixin)
	{
		if(mixin != null)
		{
			mixins().add(mixin);
		}
	}
	
	/**
	 * This is going to return a Iterator of <code>FieldAccessor</code>. Uses lazy evaluation :-)
	 * 
	 * @return	The HashMap Iterator.
	 */
	public Iterator<FieldAccessor> fieldAccessorIterator()
	{
		return metadataFieldAccessors().iterator();
	}
		
	/**
	 * Initializes the data termvector structure. This is not added to the individual
	 * fields (so that it can be changed) but is added to the composite term vector.
	 * If the data termvector has already been initialized, this operation will replace
	 * the old one and rebuild the composite term vector.
	 * FIXME:Not able to move to the MetadataBase b'coz of mixins.
	 * @param initialTermVector The initial set of terms
	 */
	public void initializeTermVector(TermVector initialTermVector)
	{
		//System.out.println("Initializing TermVector. size is " + this.size());
		
		if (compositeTermVector != null)
		{
//			dataTermVector = initialTermVector;
			
			//initialize the composite TermVector
			rebuildCompositeTermVector();
		}
//		if there is no cFMetadata then add to the composite TermVector
		else
		{
			compositeTermVector = initialTermVector;
		}
		
		// change from vikram's semantic branch
		//unscrapedTermVector.addAll(termVector);
	}

	public boolean isFilled(String attributeName)
	{
		attributeName = attributeName.toLowerCase();
		Iterator<FieldAccessor> fieldIterator = fieldAccessorIterator();
		while(fieldIterator.hasNext())
		{
			FieldAccessor fieldAccessor = fieldIterator.next();
			// getFieldName() or getTagName()??? attributeName is from TypeTagNames.java
			if(attributeName.equals(fieldAccessor.getFieldName()))
			{
				String valueString = fieldAccessor.getValueString(this);
				return (valueString != null && valueString != "null");
			}
		}
		
		//Supporting Mixins
		if(mixins() != null && mixins().size() > 0)
		{
			Iterator<Metadata> metadataIterator = mixins().iterator();
			while(metadataIterator.hasNext())
			{
				Metadata metadata = metadataIterator.next();
				fieldIterator = metadata.fieldAccessorIterator();
				while(fieldIterator.hasNext())
				{
					FieldAccessor fieldAccessor = fieldIterator.next();
					String valueString = fieldAccessor.getValueString(metadata);
					if(valueString != null && valueString != "null")
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
		}
		
		return false;
	}

	/**
	 * @return the number of non-Null fields within this metadata
	 */
	public int size() 
	{

		int size = 0;
		
		RecursiveIterator<FieldAccessor, Metadata>  fullIterator	= recursiveIteratorWithMixins();
		while (fullIterator.hasNext())
		{
			FieldAccessor fieldAccessor	= fullIterator.next();
			Metadata currentMetadata	= fullIterator.currentObject();
			//When the iterator enters the metadata in the mixins "this" in getValueString has to be
			// the corresponding metadata in mixin.
			String valueString = fieldAccessor.getValueString(currentMetadata);
			//"null" happens with mixins fieldAccessor b'coz getValueString() returns "null".
			if (valueString != null && !"null".equals(valueString))
			{
				size++;
			}
		}
		return size;
	}
	
	/**
	 * Rebuilds the composite TermVector from the individual TermVectors
	 * FIXME:Not able to move to the MetadataBase b'coz of mixins.
	 */
	public void rebuildCompositeTermVector()
	{
		//if there are no metadatafields retain the composite termvector
		//because it might have meaningful entries

		if (compositeTermVector != null)
			compositeTermVector.clear();
		else
			compositeTermVector	= new TermVector();

		RecursiveIterator<FieldAccessor, Metadata>  fullIterator	= recursiveIteratorWithMixins();
		while (fullIterator.hasNext())
		{
			FieldAccessor fieldAccessor	= fullIterator.next();
//			fieldAccessor.isScalar();
			try
			{
				Field field = fieldAccessor.getField();
				Object object = field.get(fullIterator.currentObject());
//				if((object instanceof ArrayList || object instanceof ArrayListState))
				if(!(fieldAccessor.getTagName().equals("mixins") || fieldAccessor.getTagName().equals("authors") ||
						fieldAccessor.getTagName().equals("references") ||
						fieldAccessor.getTagName().equals("citations")))
				{
					MetadataBase metadataScalar = (MetadataBase) object;
					if(metadataScalar != null)
					{
						metadataScalar.contributeToTermVector(compositeTermVector);	
					}	
				}
//				else
//					debug("Iterator passing Collections");
				
			} catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			} catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
/*
		for (FieldAccessor fieldAccessor : this)
		{
			String valueString = fieldAccessor.getValueString(this);
			if (valueString != null && !"null".equals(valueString))
			{
				compositeTermVector.addTerms(valueString, false);
			}
		}
*/
	}
	
	/**
	 * Sets the field to the specified value and wont rebuild composteTermVector
	 * @param fieldName
	 * @param value
	 */
	//TODO -- May throw exception if there is no field accessor.
	public void set(String tagName, String value)
	{
		tagName = tagName.toLowerCase();
		//Taking care of mixins
		Metadata metadata = getMetadataWhichContainsField(tagName);

		if(metadata != null)
		{
			FieldAccessor fieldAccessor = get(tagName);
			if(fieldAccessor != null && value != null && value.length()!=0)
			{
				fieldAccessor.set(metadata, value);
			}
			else 
			{
				debug("Not Able to set the field: " + tagName);
			}
		}
	}
	
	/**
	 * Setting the field to the specified value and rebuilds the composteTermVector.
	 * @param fieldName
	 * @param value
	 */
	//TODO -- May throw exception if there is no field accessor.
	public void hwSet(String tagName, String value)
	{
		tagName = tagName.toLowerCase();
		Metadata metadata = getMetadataWhichContainsField(tagName);
		
		if(metadata != null)
		{
			FieldAccessor fieldAccessor = get(tagName);
			if(fieldAccessor != null)
			{
				fieldAccessor.set(metadata, value);
				rebuildCompositeTermVector();
			}
			else 
			{
				debug("No field Accessor");
			}
			//Debugging
//			if(fieldAccessor.getFieldName() == "title")
//			{
//			String valuestring = fieldAccessor.getValueString(this);
//			System.out.println("location:"+fieldAccessor.getValueString(this));
//			}
		}
	}
	/**
	 * Returns the metadata class if it contains a Field with name
	 * NOTE: Currently should be used ONLY for mixins
	 * @param tagName
	 * @return
	 */
	public Metadata getMetadataWhichContainsField(String tagName)
	{
		HashMapArrayList<String, FieldAccessor> fieldAccessors = metadataFieldAccessors();
		
		FieldAccessor metadataFieldAccessor = fieldAccessors.get(tagName);
		if (metadataFieldAccessor != null)
		{
			return this;
		}
		//The field may be in mixin
		if(mixins() != null && mixins().size() > 0)
		{
			for (Metadata mixinMetadata : mixins())
			{
				fieldAccessors 	= mixinMetadata.metadataFieldAccessors();
				FieldAccessor mixinFieldAccessor 	= fieldAccessors.get(tagName);
				if(mixinFieldAccessor != null)
				{
					return mixinMetadata;
				}
			}
		}
		return null;
	}
	
//	public void setMixinField(String tagName, String value)
//	{
//		tagName = tagName.toLowerCase();
//		Metadata metadata = getMetadataWhichhasField(tagName); 
//		
//		if(metadata != null)
//		{
//			HashMapArrayList<String, FieldAccessor> fieldAccessors = Optimizations.getFieldAccessors(metadata.getClass());
//			FieldAccessor fieldAccessor = fieldAccessors.get(tagName);
//			if(fieldAccessor != null)
//			{
//				fieldAccessor.set(metadata, value);
//			}
//			else 
//			{
//				System.out.println("No field Accessor");
//				//fieldAccessor.set(this, value);
//			}
//		}
//	}
	
	
	
	public Field getFields()
	{
		
		return null;
	}
	
	public MetaMetadataField childMetaMetadata(String name)
	{
		return metaMetadata == null ? null : metaMetadata.lookupChild(name);
	}

	public MetaMetadata getMetaMetadata()
	{
		return metaMetadata;
	}

	public void setMetaMetadata(MetaMetadata metaMetadata)
	{
		this.metaMetadata = metaMetadata;
	}
	
	public void initializeMetadataCompTermVector()
	{
		compositeTermVector = new TermVector();
	}
	
	public boolean loadedFromPreviousSession()
	{
		return loadedFromPreviousSession;
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
	public ArrayListState<Metadata> getMixins()
	{
		return mixins();
	}

	/**
	 * @param mixins the mixins to set
	 */
	public void setMixins(ArrayListState<Metadata> mixins)
	{
		this.mixins = mixins;
	}
	
	/**
	 * Efficiently retrieve appropriate MetadataFieldAccessor, using lazy evaluation.
	 * 
	 * @param fieldName
	 * @return
	 */
	public MetadataFieldAccessor getMetadataFieldAccessor(String fieldName)
	{
		return (MetadataFieldAccessor) metadataFieldAccessors().get(fieldName);
	}
	
	//For adding mapped attributes
	public void add(String key)
	{
		
	}
	
	public Metadata get(int index)
	{
		return null;
	}
	
	public RecursiveIterator<FieldAccessor, Metadata> recursiveIteratorWithMixins()
	{
		return new RecursiveIterator<FieldAccessor, Metadata>(this, 
				(mixins == null) ? null : mixins.iterator());
	}

	
	public static class RecursiveIterator<I, O extends Iterable<I>>
	implements Iterator<I>
	{
		private Iterator<I> firstIterator;
		private Iterator<O> collection;
		
		private O			currentObject;
		
		private Iterator<I>	currentIterator;
		
		public RecursiveIterator(O firstObject, Iterator<O> iterableCollection)
		{
			this.firstIterator	= firstObject.iterator();
			this.currentObject	= firstObject;
			this.collection	= iterableCollection;
		}
		
		private boolean collectionHasNext()
		{
			return collection != null && (collection.hasNext() || currentHasNext());
		}

		private boolean currentHasNext() 
		{
			return (currentIterator != null) && currentIterator.hasNext();
		}
		
		public boolean hasNext()
		{
			return firstIterator.hasNext() || collectionHasNext();
		}

		public I next() 
		{
			if (firstIterator.hasNext())
				return firstIterator.next();
			// else
			if (currentHasNext())
				return currentIterator.next();
			// else
			if (collectionHasNext())
			{
				currentObject		= collection.next();
				currentIterator		= currentObject.iterator();
				return currentIterator.next();
			}
			return null;
		}
		
		public O currentObject()
		{
			return currentObject;
		}

		public void remove() 
		{
			throw new UnsupportedOperationException();
		}
		
	}
}
