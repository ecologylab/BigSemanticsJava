package ecologylab.bigsemantics.metametadata;

import java.util.Date;

import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Build information for the mmd repository.
 *
 * @author quyin
 */
public class Build
{
  
  @simpl_scalar
  public Date date;
  
  @simpl_scalar
  public String host;
  
  @simpl_scalar
  public String user;

  public void copyFrom(Build build)
  {
    this.date = build.date;
    this.host = build.host;
    this.user = build.user;
  }

}
