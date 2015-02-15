package ecologylab.bigsemantics.logging;

import java.util.Date;

import ecologylab.logging.LogPost;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.types.element.IMappable;

/**
 * 
 * @author quyin
 */
public class Phase implements IMappable<String>
{

  // predefined names:

  public static final String WHOLE              = "whole";

  public static final String DOWNLOAD_AND_PARSE = "download_and_parse";

  public static final String PCACHE_READ        = "persistent_cache_read";

  public static final String PCACHE_WRITE       = "persistent_cache_write";

  public static final String DOWNLOAD           = "download";

  public static final String EXTRACT            = "extract";

  @simpl_scalar
  String                     name;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  Date                       beginTime;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  Date                       endTime;

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  long                       timeInMs;

  @simpl_composite
  LogPost                    logPost;

  public LogPost getLogPost()
  {
    return logPost;
  }

  public LogPost logPost()
  {
    if (logPost == null)
    {
      synchronized (this)
      {
        if (logPost == null)
        {
          logPost = new LogPost();
        }
      }
    }
    return logPost;
  }

  @Override
  public String key()
  {
    return name;
  }

}
