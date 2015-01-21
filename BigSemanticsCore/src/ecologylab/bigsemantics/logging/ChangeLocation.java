package ecologylab.bigsemantics.logging;

import ecologylab.logging.LogEvent;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
@simpl_inherit
public class ChangeLocation extends LogEvent
{

  @simpl_scalar
  ParsedURL from;
  
  @simpl_scalar
  ParsedURL to;

  public ChangeLocation()
  {
    this(null, null);
  }

  public ChangeLocation(ParsedURL from, ParsedURL to)
  {
    super();
    this.from = from;
    this.to = to;
  }
  
}
