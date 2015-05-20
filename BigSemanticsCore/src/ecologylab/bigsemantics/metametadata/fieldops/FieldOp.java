package ecologylab.bigsemantics.metametadata.fieldops;

/**
 * Provides a general way of operating on raw extraction strings for a field. This operation should
 * happen before field parsers.
 * 
 * @author quyin
 */
public interface FieldOp
{

  /**
   * @param rawValue
   *          The raw extraction result.
   * @return New (modified) extraction result.
   */
  Object operateOn(Object rawValue) throws Exception;

  /**
   * @return A serialized form of this FieldOp. Used as the fingerprint string of this object.
   */
  String toString();
  
}
