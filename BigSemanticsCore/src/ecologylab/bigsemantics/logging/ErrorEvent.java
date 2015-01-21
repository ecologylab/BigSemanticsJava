package ecologylab.bigsemantics.logging;

import ecologylab.bigsemantics.Utils;
import ecologylab.logging.LogEvent;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
@simpl_inherit
public class ErrorEvent extends LogEvent
{

  @simpl_scalar
  String message;

  public ErrorEvent()
  {
    this((String) null);
  }

  public ErrorEvent(Throwable throwable)
  {
    this(throwable == null ? null : Utils.getStackTraceAsString(throwable));
  }

  public ErrorEvent(String message)
  {
    super();
    this.message = message;
  }

}
