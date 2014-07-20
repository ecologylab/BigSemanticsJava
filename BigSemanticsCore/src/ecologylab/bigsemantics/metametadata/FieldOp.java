package ecologylab.bigsemantics.metametadata;

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
   *          The raw extraction results, as a string.
   * @return New string as the extraction result.
   */
  String operateOn(String rawValue);

  /**
   * @return A serialized form of this FieldOp. Used as the fingerprint string of this object.
   */
  String toString();
  
}
