package ecologylab.semantics.metadata;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.ReflectionTools;
import ecologylab.model.ParticipantInterest;
import ecologylab.model.text.TermVector;
import ecologylab.model.text.WordForms;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.xml.ElementState;
import ecologylab.xml.Optimizations;
import ecologylab.xml.ElementState.xml_leaf;

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
	
	TermVector 				compositeTermVector;
	
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

	@xml_leaf
	String context;
	
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
		if(compositeTermVector != null && !compositeTermVector.isEmpty())
		{
			// first modify the composite TermVector
			compositeTermVector.incrementParticipantInterest(delta);
			
			//TODO Sashikanth: iterate on child fields
			Iterator it = iterator();
			while (it.hasNext())
			{
				
				// TermVectors
				Metadata mData = (Metadata) it.next();
				mData.termVector().incrementParticipantInterest(delta);
				
				// Lastly the actual fields
				mData.incrementParticipantInterest(delta);
			}
		}
		
		
	}
	
	/**
	 * Get an Iterator for iterating the HashMap.
	 * @return	The HashMap Iterator.
	 */
	public Iterator iterator()
	{
		//TODO Sashikanth: Figure out how to iterate through the fields 
		//within this metadata object and return the appropriate Iterator

		return null;
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
		
		if (this.size() > 0)
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

	public int size() 
	{
		// TODO Sashikanth: Use Reflection to get the number of fields 
		//of the instantiated metadata object
		return 0;
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
		if (this.size() > 0)
		{
			if (compositeTermVector != null)
				compositeTermVector.clear();
			else
				compositeTermVector	= new TermVector();
//			termVector.clear();
			
			
//			Optimizations rootOptimizations = Optimizations.lookupRootOptimizations(this);
//			ArrayList<Field> fields = rootOptimizations.getFields();
			
			//TODO Sashikanth: Iterate on Child Fields
			Iterator it = iterator();
			while (it.hasNext())
			{
				
				Metadata mData = (Metadata) it.next();
				if (mData.compositeTermVector != null)
				{
					TermVector fieldTermVector = mData.termVector();
					compositeTermVector.combine(fieldTermVector);
				}
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
	
	public Field getFields()
	{
		
		return null;
	}
	
	public MetaMetadataField childMetaMetadata(String name)
	{
		return metaMetadata == null ? null : metaMetadata.lookupChild(name);
	}

	public String getContext()
	{
		return context;
	}

	public void setContext(String context)
	{
		this.context = context;
	}

	public MetaMetadata getMetaMetadata()
	{
		return metaMetadata;
	}

	public void setMetaMetadata(MetaMetadata metaMetadata)
	{
		this.metaMetadata = metaMetadata;
	}
}
