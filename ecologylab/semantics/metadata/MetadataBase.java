/**
 * 
 */
package ecologylab.semantics.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ecologylab.generic.HashMapArrayList;
import ecologylab.generic.OneLevelNestingIterator;
import ecologylab.semantics.metametadata.MetaMetadata;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.model.text.ITermVector;
import ecologylab.xml.ClassDescriptor;
import ecologylab.xml.ElementState;
import ecologylab.xml.FieldDescriptor;
import ecologylab.xml.ScalarUnmarshallingContext;
import ecologylab.xml.serial_descriptors_classes;

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
