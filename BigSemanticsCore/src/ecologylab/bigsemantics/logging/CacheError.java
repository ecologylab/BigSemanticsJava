package ecologylab.bigsemantics.logging;

import ecologylab.serialization.annotations.simpl_inherit;

/**
 * 
 * @author quyin
 */
@simpl_inherit
public class CacheError extends ErrorEvent
{

  public CacheError()
  {
    super();
  }

  public CacheError(Throwable throwable)
  {
    super(throwable);
  }

  public CacheError(String message)
  {
    super(message);
  }

}
