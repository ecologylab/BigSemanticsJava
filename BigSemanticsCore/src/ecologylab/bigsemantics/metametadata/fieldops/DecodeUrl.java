package ecologylab.bigsemantics.metametadata.fieldops;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * A field op that allows one to extract query string from another URL.
 * 
 * @author quyin
 */
public class DecodeUrl implements FieldOp
{

  @Override
  public Object operateOn(Object rawValue) throws UnsupportedEncodingException
  {
    return rawValue == null ? null : URLDecoder.decode(rawValue.toString(), "UTF-8");
  }

}
