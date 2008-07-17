/**
 * 
 */
package ecologylab.semantics.metadata;

import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.model.ParticipantInterest;
import ecologylab.model.text.TermVector;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldAccessor;
import ecologylab.xml.Optimizations;

/**
 * Base class for Metadata fields that represent scalar values.
 * 
 * These, for example, lack mixins.
 * 
 * @author andruid
 *
 */
public class MetadataBase extends ElementState implements Iterable<FieldAccessor>
{
	
	protected TermVector 				compositeTermVector;
	
	/**
	 * Represents interest in this field as a whole
	 * (Example: author = Ben Shneiderman), in addition to interest
	 * propagated into the termVector, which represents interest in
	 * particular Terms. While the latter is the basis of the information
	 * retrieval model (IR), this is what enables the model to function
	 * in the context of the semantic web / digital libraries.
	 */  
	ParticipantInterest				participantInterest = new ParticipantInterest();

	HashMapArrayList<String, FieldAccessor> metadataFieldAccessors;


	/**
	 * 
	 */
	public MetadataBase()
	{
		// TODO Auto-generated constructor stub
	}


	/**
	 * @return the compositeTermVector
	 */
	public TermVector getCompositeTermVector()
	{
		return compositeTermVector;
	}


	/**
	 * @param compositeTermVector the compositeTermVector to set
	 */
	public void setCompositeTermVector(TermVector compositeTermVector)
	{
		this.compositeTermVector = compositeTermVector;
	}


	/**
	 * @return the participantInterest
	 */
	public ParticipantInterest getParticipantInterest()
	{
		return participantInterest;
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


	/**
	 * @param participantInterest the participantInterest to set
	 */
	public void setParticipantInterest(ParticipantInterest participantInterest)
	{
		this.participantInterest = participantInterest;
	}


	protected HashMapArrayList<String, FieldAccessor> metadataFieldAccessors()
	{
		HashMapArrayList<String, FieldAccessor> result	= this.metadataFieldAccessors;
		if (result == null)
		{
			result			= Optimizations.getFieldAccessors(this.getClass(), MetadataFieldAccessor.class);
			metadataFieldAccessors	= result;
		}
		return result;
	}


	public Iterator<FieldAccessor> iterator()
	{
		return metadataFieldAccessors().iterator();
	}

	public FieldAccessor get(String key)
	{
		HashMapArrayList<String, FieldAccessor> fieldAccessors = metadataFieldAccessors();
		return fieldAccessors.get(key);
	}
	

}
