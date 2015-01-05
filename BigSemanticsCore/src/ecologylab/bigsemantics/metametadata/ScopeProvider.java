package ecologylab.bigsemantics.metametadata;

/**
 * Something that associates with a scope.
 * 
 * @author quyin
 */
public interface ScopeProvider
{

  /**
   * @return The scope. Can be null.
   */
  MmdScope getScope();

  /**
   * @return The scope. Create one if necessary. Cannot be null.
   */
  MmdScope scope();

  /**
   * @param scope
   *          The scope to be associated with this object.
   */
  void setScope(MmdScope scope);

}
