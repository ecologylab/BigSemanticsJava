/**
 * 
 */
package ecologylab.semantics.metadata;

import ecologylab.semantics.model.text.ITermVector;

/**
 * Base class for Metadata fields that represent scalar values.
 * 
 * These, for example, lack mixins.
 * 
 * @author andruid
 *
 */
public interface MetadataBase
extends Iterable<MetadataFieldDescriptor>
{
	
	/**
	 * Only use this accessor, in order to maintain future code compatability.
	 * 
	 * @return
	 */
//	public MetaMetadataRepository repository();

	/**
	 * This is actually the real composite term vector.
	 * 
	 * @return	Null for scalars.
	 */
	public ITermVector termVector();


	public void recycle();

	

	/**
	 * Rebuilds the composite TermVector from the individual TermVectors, when there is one.
	 * This implementation, in the base class, does nothing.
	 */
	public void rebuildCompositeTermVector();

	/**
	 * Determine if the Metadata has any entries.
	 * @return	True if there are Metadata entries.
	 */
	public boolean hasCompositeTermVector();

  	/**
  	 * 
  	 * @param context
  	 *          Object that the field is in.
  	 * 
  	 * @return true if the field is not a scalar or a psuedo-scalar, and it has a non null value.
  	 */
//  	public boolean isNonNullReference(ElementState context);

}
