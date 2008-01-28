package ecologylab.semantics.metadata;

import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.model.ParticipantInterest;
import ecologylab.model.text.TermVector;
import ecologylab.model.text.WordForms;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.xml.ElementState;

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
	
	HashMapArrayList<String, MetaMetadata> metaMetadataMap = new HashMapArrayList<String, MetaMetadata>();
	
	TermVector 				termVector;
	
	/**
    * Stores a reference to the <code>Metadata</code> that refers to this Metadata.
    */
	Metadata				metadataReferer;
	
	/**
	 * Represents interest in this field as a whole
	 * (Example: author = Ben Shneiderman), in addition to interest
	 * propagated into the termVector, which represents interest in
	 * particular Terms. While the latter is the basis of the information
	 * retrieval model (IR), this is what enables the model to function
	 * in the context of the semantic web / digital libraries.
	 */  
	ParticipantInterest		participantInterest = new ParticipantInterest();
	
	
	public TermVector termVector()
	{
		return termVector;
	}
   
   /**
    * Increments interest in this MetadataField. 
    * @param delta	The change in interest.
    */
	public void incrementParticipantInterest(short delta)
	{
		participantInterest.increment(delta);
	}
	
	public Metadata metadataReferer()
	{
		return this.metadataReferer;
	}
	
	public void recycle()
	{
		//termVector.clear();
		termVector 				= null;
		metadataReferer 		= null;
		participantInterest		= null;
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
			termVector.add(wf);
		}
	}
	
	public MetaMetadata getMetaMetadata()
	{
		return metaMetadata;
	}
	
	public MetaMetadata getMetaMetadata(String metadataName)
	{
		MetaMetadata mmData;
		
		if(metadataName == this.getClassName())
			mmData = metaMetadata;
		else
		{
			mmData = metaMetadataMap.get(metadataName);
		}
		
		return mmData;
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
		// first modify the composite TermVector
		termVector.incrementParticipantInterest(delta);
		
		// then the individual TermVectors and fields
		Iterator it = iterator();
		while (it.hasNext())
		{
			//TODO Sashikanth: Iteration will be something like T extends Metadata
			// TermVectors
			Metadata mData = (Metadata) it.next();
			mData.termVector().incrementParticipantInterest(delta);
			
			// Lastly the actual fields
			mData.incrementParticipantInterest(delta);
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
			termVector = initialTermVector;
		}
		
		// change from vikram's semantic branch
		//unscrapedTermVector.addAll(termVector);
	}

	private int size() {
		// TODO Sashikanth: Use Reflection to get the number of fields 
		//of the instantiated metadata object
		return 0;
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
			if (termVector != null)
				termVector.clear();
			else
				termVector	= new TermVector();
//			termVector.clear();
			
			Iterator it = iterator();
			while (it.hasNext())
			{
				//TODO Sashikanth: Iteration will be something like T extends Metadata
				Metadata mData = (Metadata) it.next();
				if (mData.termVector != null)
				{
					TermVector fieldTermVector = mData.termVector();
					termVector.combine(fieldTermVector);
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
		return termVector.getWeight();
	}
	
	/**
	 * The lnWeight
	 * @return	The lnWeight() of the composite TermVector.
	 * @see ecologylab.model.text.TermVector#lnWeight()
	 */
	public float lnWeight()
	{
		return termVector.lnWeight();
	}
	
}
