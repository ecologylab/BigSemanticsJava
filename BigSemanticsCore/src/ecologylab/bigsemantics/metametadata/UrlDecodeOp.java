package ecologylab.bigsemantics.metametadata;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * A field op that allows one to extract query string from another URL.
 * 
 * @author quyin
 */
@simpl_inherit
public class UrlDecodeOp implements FieldOp
{

  public static final String URL_LEGAL_SYMBOLS = "-._~:/?#[]@!$&'()*+,;=";

  static Logger              logger            = LoggerFactory.getLogger(UrlDecodeOp.class);

  /**
   * By default, this op tries to detect if the input string is actually encoded, and if not, it
   * will not do decoding. If this field is set to true, this op will do decoding anyway.
   */
  @simpl_scalar
  private boolean            force;

  @Override
  public String operateOn(String rawValue)
  {
    if (!force)
    {
      // if the raw value contains illegal characters, it must be
      // not-yet-encoded, so we don't do decoding.
      for (int i = 0; i < rawValue.length(); ++i)
      {
        char c = rawValue.charAt(i);
        if (!Character.isLetterOrDigit(c) && URL_LEGAL_SYMBOLS.indexOf(c) < 0 && c != '%')
        {
          return rawValue;
        }
      }
    }

    try
    {
      return URLDecoder.decode(rawValue, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      logger.error("UTF-8 codec not avaiable?!", e);
    }

    return rawValue;
  }

}
