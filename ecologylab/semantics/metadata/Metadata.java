package ecologylab.semantics.metadata;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.model.ParticipantInterest;
import ecologylab.model.text.TermVector;
import ecologylab.model.text.WordForms;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.Optimizations;
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
abstract public class Metadata extends ElementState
{
	MetaMetadata 			metaMetadata;
	
	/**
	 * Helps in the user annotation.
	 */
	@xml_nested	ArrayListState<Metadata>	mixins;
	
	//FIXME -- not public!
	public TermVector 				compositeTermVector;
	
	final static int		INITIAL_SIZE		= 5;

	/**
	 * Represents interest in this field as a whole
	 * (Example: author = Ben Shneiderman), in addition to interest
	 * propagated into the termVector, which represents interest in
	 * particular Terms. While the latter is the basis of the information
	 * retrieval model (IR), this is what enables the model to function
	 * in the context of the semantic web / digital libraries.
	 */  
	ParticipantInterest		participantInterest = new ParticipantInterest();

	/**
	 * Set to true if this cFMetadata object was restored from a saved collage.
	 * This is necessary to prevent cFMetadata from being added again and hence
	 * overwritting edited cFMetadata when the elements are recrawled on a restore.
	 */
	private boolean loadedFromPreviousSession 	= false;
	
	public Metadata()
	{
		
	}
	
	public Metadata(MetaMetadata metaMetadata)
	{
		this.metaMetadata		= metaMetadata;
	}
//	public Metadata(boolean createTermVector)
//	{
//		if(createTermVector)
//			compositeTermVector = new TermVector();
//	}
	
	public TermVector termVector()
	{
		return compositeTermVector;
	}
   
   /**
    * Increments interest in this MetadataField. 
    * @param delta	The change in interest.
    */
	public void incrementParticipantInterest(short delta)
	{
		participantInterest.increment(delta);
	}
	
	public void recycle()
	{
		//termVector.clear();
		compositeTermVector 				= null;
		participantInterest					= null;
	}
	
	/**
    * Append the term to the field "value" as well as the term vector.
    * 
    * @param wf The term to add.
    */
	public void addTerm(WordForms wf)
	{
		compositeTermVector.add(wf);
		   
		//value = null; //force value to be updated later
		   
		//if (termVector.size() > 0)
		   
		//this concat is way cheaper than rebuilding the termvector toString().
		//value			= value.toString() + ' ' + wf.string();
		   
		//this.value = termVector.toString();
		//this.value = termVector.toString(termVector.size(), ' ');
		//else
		//	   this.value = "";
		
		compositeTermVector.combine(compositeTermVector, false);
	}
	   
	/**
	* Append terms to the field "value" as well as the term vector.
	* 
	* @param wfv The vector of terms to add.
	*/
	public void addTerm(TermVector wfv)
	{
		Iterator it = wfv.iterator();
		while (it.hasNext())
		{ 
			WordForms wf = (WordForms) it.next();
			compositeTermVector.add(wf);
		}
	}
	
	/**
	 * Modifies interest by delta. This function modifies the interest
	 * in the composite TermVector, the constituent individual TermVectors,
	 * and the interest in actual fields themselves (for the semantic web/DLs) 
	 * 
	 * @param delta		The amount to modify interest
	 */
	public void incrementInterest(short delta)
	{
		//Metadata Transition --bharat
		if(compositeTermVector != null && !compositeTermVector.isEmpty())
		{
			compositeTermVector.incrementParticipantInterest(delta);
			incrementParticipantInterest(delta);
		}
//		if(compositeTermVector != null && !compositeTermVector.isEmpty())
//		{
//			// first modify the composite TermVector
//			compositeTermVector.incrementParticipantInterest(delta);
//			
//			//TODO Sashikanth: iterate on child fields
//			Iterator it = iterator();
//			while (it.hasNext())
//			{
//				
//				// TermVectors
//				Metadata mData = (Metadata) it.next();
//				mData.termVector().incrementParticipantInterest(delta);
//				
//				// Lastly the actual fields
//				mData.incrementParticipantInterest(delta);
//			}
//		}
		
		
	}
	
	/**
	 * This is going to return a Iterator of <code>FieldAccessor</code>
	 * 
	 * @return	The HashMap Iterator.
	 */
	//Metadata Transition --bharat
	public Iterator<FieldAccessor> fieldIterator()
	{
		//TODO Sashikanth: Figure out how to iterate through the fields 
		//within this metadata object and return the appropriate Iterator
		HashMapArrayList<String, FieldAccessor> fieldAccessors = Optimizations.getFieldAccessors(this.getClass());
		
		Iterator<FieldAccessor> fieldIterator = fieldAccessors.iterator();
		return fieldIterator;
//		return null;
	}
	
	
	/**
	 * Initializes the data termvector structure. This is not added to the individual
	 * fields (so that it can be changed) but is added to the composite term vector.
	 * If the data termvector has already been initialized, this operation will replace
	 * the old one and rebuild the composite term vector.
	 * 
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
		Iterator<FieldAccessor> fieldIterator = fieldIterator();
		while(fieldIterator.hasNext())
		{
			FieldAccessor fieldAccessor = fieldIterator.next();
			// getFieldName() or getTagName()??? attributeName is from TypeTagNames.java
			if(attributeName.equals(fieldAccessor.getFieldName()))
			{
				String valueString = fieldAccessor.getValueString(this);
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
		return false;
	}
	
	//Metadata Transition --bharat
	public int size() 
	{
		// TODO Sashikanth: Use Reflection to get the number of fields 
		//of the instantiated metadata object
		int size = 0;
		
		

		Iterator<FieldAccessor> fieldIterator = fieldIterator();
		while(fieldIterator.hasNext())
		{
			FieldAccessor fieldAccessor = fieldIterator.next();
			String valueString = fieldAccessor.getValueString(this);
			if(valueString != null && valueString != "null")
			{
				System.out.println("field:"+fieldAccessor.getFieldName()+ " value:"+valueString);
				size++;
			}
		}
		return size;

	}
	
	/**
	 * Determine if the Metadata has any entries.
	 * @return	True if there are Metadata entries.
	 */
	public boolean hasCompositeTermVector()
	{
		return (compositeTermVector != null);
	}
	/**
	 * Rebuilds the composite TermVector from the individual TermVectors
	 */
	public void rebuildCompositeTermVector()
	{
		//if there are no metadatafields retain the composite termvector
		//because it might have meaningful entries

		if (compositeTermVector != null)
			compositeTermVector.clear();
		else
			compositeTermVector	= new TermVector();
//		termVector.clear();

		Iterator<FieldAccessor> fieldIterator = fieldIterator();
		while(fieldIterator.hasNext())
		{
			FieldAccessor fieldAccessor = fieldIterator.next();
			String valueString = fieldAccessor.getValueString(this);
			if(valueString != null && valueString != "null")
			{
				compositeTermVector.addTerms(valueString, false);
			}
		}


		//add any actual data terms to the composite term vector
//		if (dataTermVector != null)
//			termVector.combine(dataTermVector);
		
	}
	
	
	/**
	 * The weight of the composite TermVector.
	 * @return	The composite TermVector's weight.
	 * @see ecologylab.model.text.TermVector#getWeight()
	 */
	public float getWeight()
	{
		return compositeTermVector == null ? 1 : compositeTermVector.getWeight();
	}
	
	/**
	 * The lnWeight
	 * @return	The lnWeight() of the composite TermVector.
	 * @see ecologylab.model.text.TermVector#lnWeight()
	 */
	public float lnWeight()
	{
		return compositeTermVector == null ? 0 : compositeTermVector.lnWeight();
	}
	/**
	 * Setting the field to the specified value and rebuilds the composteTermVector.
	 * @param fieldName
	 * @param value
	 */
	//Metadata TransitionTODO -- May throw exception if there is no field accessor.
	public void set(String tagName, String value)
	{
		//Metadata TransitionTODO --Must have similar changes as lwSet
		tagName = tagName.toLowerCase();
		HashMapArrayList<String, FieldAccessor> fieldAccessors = Optimizations.getFieldAccessors(this.getClass());
		FieldAccessor fieldAccessor = fieldAccessors.get(tagName);
		fieldAccessor.set(this, value);
		if(fieldAccessor.getFieldName() == "title")
		{
			String valuestring = fieldAccessor.getValueString(this);
			System.out.println("location:"+fieldAccessor.getValueString(this));
		}
		rebuildCompositeTermVector();
	}
	/**
	 * Sets the field to the specified value and wont rebuild composteTermVector
	 * @param fieldName
	 * @param value
	 */
	//Metadata Transition -- TODO -- May throw exception if there is no field accessor.
	public void lwSet(String fieldName, String value)
	{
		//In PDFTypeMultiAndBox.java this method is called with "Author"
		fieldName = fieldName.toLowerCase();
		HashMapArrayList<String, FieldAccessor> fieldAccessors = Optimizations.getFieldAccessors(this.getClass());
		FieldAccessor fieldAccessor = fieldAccessors.get(fieldName);
//		fieldAccessor.set(this, value);
		if(fieldAccessor != null)
		{
			fieldAccessor.set(this, value);
		}
		else 
		{
			System.out.println("No field Accessor");
			//fieldAccessor.set(this, value);
		}
			
	}
	
	public ParsedURL getLocation()
	{
		return null;
	}
	
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
	
	//Metadata Transition --bharat
	public void initializeMetadataCompTermVector()
	{
		compositeTermVector = new TermVector();
	}
	
	public boolean loadedFromPreviousSession()
	{
		return loadedFromPreviousSession;
	}
	
}
