package ecologylab.semantics.metametadata;

import java.util.Collection;
import java.util.Iterator;

import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.MetadataBase;
import ecologylab.semantics.metadata.MetadataFieldDescriptor;

/**
 * Iterates through a Collection of things, and then through an Iterator
 * of such (nested) Collections of things.
 * Provides flat access to all members.
 * 
 * @author jmole, damaraju
 *
 * @param <I>   Class that we iterate over.
 * @param <O>   Class of objects that are applied in the context of what we iterate over.
 *          This typically starts as this, but shifts as we iterate through 
 *          the nested Collection of Iterators.
 */
public class ClassAndCollectionIterator
implements Iterator<MetadataBase>
{
  private Iterator<MetaMetadataField> iterator;
  private Iterator<MetadataBase> collectionIterator;
  private MetaMetadataField root;
  private MetadataBase currentObject;
  
  private MetaMetadataField currentMMField;

	private Metadata metadata;

  /**
   * 
   * @param firstObject - The object whose elements need to be iterated over.
   */
  public ClassAndCollectionIterator(MetaMetadataField firstObject, Metadata metadata)
  {
    root = firstObject;
    this.iterator  = firstObject.iterator();
    this.metadata = metadata;
  }

  /**
   * @return The next field in the Object.<br>
   * If the next object is a non-null collection, it iterates through the objects of that collection 
   */
  public MetadataBase next() 
  {
    try
    {
      if (collectionIterator != null)
        return nextInCollection();
      
      if (iterator.hasNext())
      {
      	MetaMetadataField firstNext = iterator.next(); 
      	currentMMField = firstNext;
        if (firstNext instanceof MetaMetadataCollectionField)
        {
        	MetadataFieldDescriptor mfd = firstNext.getMetadataFieldDescriptor();
            Collection<MetadataBase> c = mfd.getCollection(metadata);
            if (c != null)
        	{
            	collectionIterator = c.iterator();
            	return nextInCollection();
        	}
        }
        MetadataFieldDescriptor mfd = firstNext.getMetadataFieldDescriptor();
        if (mfd == null)
        	firstNext.error("Can't find MetadataFieldDescriptor. This probably means that the MetaMetadata compiler was not run or encountered errors!");
        MetadataBase md = (MetadataBase) mfd.getField().get((MetadataBase)metadata);
        currentObject = md;
        return md;
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return null;
  }

  private MetadataBase nextInCollection() 
  {
    if (!collectionIterator.hasNext()) {
      collectionIterator = null;
      return next();
    } 
    MetadataBase next = collectionIterator.next();
    currentObject = next;
    return next;
  }

  /**
   * 
   * @return
   */
  public MetadataBase currentObject()
  {
    return currentObject;
  }

  public void remove() 
  {
    throw new UnsupportedOperationException();
  }

  public boolean hasNext()
  {    
    return iterator.hasNext() || (collectionIterator != null && collectionIterator.hasNext());
  }
  
  
  public MetaMetadataField getCurrentMMField()
	{
		return currentMMField;
	}
}
