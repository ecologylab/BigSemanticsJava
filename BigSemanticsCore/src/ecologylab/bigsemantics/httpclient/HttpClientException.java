package ecologylab.bigsemantics.httpclient;

/**
 * 
 * @author quyin
 */
public class HttpClientException extends Exception
{

  private static final long serialVersionUID = -3754068242184961467L;

  public HttpClientException()
  {
    super();
  }

  public HttpClientException(String message)
  {
    super(message);
  }

  public HttpClientException(Throwable cause)
  {
    super(cause);
  }

  public HttpClientException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public HttpClientException(String message,
                             Throwable cause,
                             boolean enableSuppression,
                             boolean writableStackTrace)
  {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
