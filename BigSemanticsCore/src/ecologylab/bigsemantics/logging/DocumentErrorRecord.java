package ecologylab.bigsemantics.logging;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
public class DocumentErrorRecord
{

  @simpl_scalar
  String message;

  @simpl_scalar
  String stacktrace;

  public DocumentErrorRecord()
  {
    this(null, null);
  }

  public DocumentErrorRecord(String message, String stacktrace)
  {
    this.message = message;
    this.stacktrace = stacktrace;
  }

}
