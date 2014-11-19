package ecologylab.bigsemantics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import ecologylab.generic.StringBuilderBaseUtils;
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

  private static final int    BUF_SIZE = 1024;

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
    if (obj == null)
    {
      return null;
    }
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

  public static Charset getCharsetByName(String charsetName, Charset defaultCharset)
  {
    if (charsetName == null)
    {
      return defaultCharset;
    }
    try
    {
      return Charset.forName(charsetName);
    }
    catch (Exception e)
    {
      logger.error("Unknown charset: " + charsetName, e);
    }
    return defaultCharset;
  }

  public static String readInputStream(InputStream inputStream) throws IOException
  {
    return readInputStream(inputStream, Charsets.UTF_8);
  }

  public static String readInputStream(InputStream inputStream, Charset charset) throws IOException
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset));
    StringBuilder sb = StringBuilderBaseUtils.acquire();
    char[] buf = new char[BUF_SIZE];
    while (true)
    {
      int n = br.read(buf, 0, BUF_SIZE);
      if (n < 0)
      {
        break;
      }
      sb.append(buf, 0, n);
    }
    String result = sb.toString();
    StringBuilderBaseUtils.release(sb);
    return result;
  }

  public static void writeToFile(File dest, String content) throws IOException
  {
    FileWriter fw = null;
    try
    {
      fw = new FileWriter(dest);
      fw.write(content);
    }
    catch (IOException e)
    {
      logger.error("Cannot write to file: " + dest, e);
      throw e;
    }
    finally
    {
      if (fw != null)
      {
        fw.close();
      }
    }
  }

}
