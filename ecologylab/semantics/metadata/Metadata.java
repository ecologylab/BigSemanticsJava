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
	
	HashMapArrayList<Class<? extends Metadata>, MetaMetadata> metaMetadataMap;
	
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
}
