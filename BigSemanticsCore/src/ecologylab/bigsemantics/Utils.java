package ecologylab.bigsemantics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * Utility functions.
 * 
 * @author quyin
 */
public class Utils
{

  private static Logger       logger;

  private static HashFunction fingerprintHashFunc;

  private static HashFunction secureHashFunc;

  private static BaseEncoding base64Encoder;

  static
  {
    logger = LoggerFactory.getLogger(Utils.class);
    fingerprintHashFunc = Hashing.goodFastHash(32);
    secureHashFunc = Hashing.sha256();
    base64Encoder = BaseEncoding.base64Url();
  }

  /**
   * Get the fingerprint of an input string. A fingerprint is not secure hash.
   * 
   * @param s
   * @return
   */
  public static byte[] fingerprintBytes(String s)
  {
    return fingerprintHashFunc.hashString(s).asBytes();
  }

  /**
   * Get a secure hash of the input string.
   * 
   * @param s
   * @return
   */
  public static byte[] secureHashBytes(String s)
  {
    return secureHashFunc.hashString(s, Charsets.UTF_8).asBytes();
  }

  /**
   * Encode the input bytes in base64.
   * 
   * @param bytes
   * @return
   */
  public static String base64urlEncode(byte[] bytes)
  {
    return base64Encoder.encode(bytes);
  }

  public static String serializeToString(Object obj, StringFormat format)
  {
    try
    {
      return SimplTypesScope.serialize(obj, format).toString();
    }
    catch (SIMPLTranslationException e)
    {
      logger.error("Cannot serialize " + obj, e);
    }
    return "ERROR";
  }

}
