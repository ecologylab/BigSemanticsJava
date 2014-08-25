package ecologylab.bigsemantics.metametadata;

import ecologylab.collections.MultiAncestorScope;

/**
 * Something that associates with a scope.
 * 
 * @author quyin
 */
public interface ScopeProvider<T>
{

  /**
   * @return The scope. Can be null.
   */
  MultiAncestorScope<T> getScope();

  /**
   * @return The scope. Create one if necessary. Cannot be null.
   */
  MultiAncestorScope<T> scope();

  /**
   * @param scope
   *          The scope to be associated with this object.
   */
  void setScope(MultiAncestorScope<T> scope);

}
