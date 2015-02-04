package ecologylab.bigsemantics.httpclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

/**
 * Fixing redirect handling in Apache HttpClient.
 * 
 * @author quyin
 */
class RedirectHttpClient extends DefaultHttpClient
{

  private final Log log = LogFactory.getLog("org.apache.http.impl.client.RedirectHttpClient");

  public RedirectHttpClient(ClientConnectionManager conman)
  {
    super(conman);
  }

  @Override
  protected RequestDirector createClientRequestDirector(HttpRequestExecutor requestExec,
                                                        ClientConnectionManager conman,
                                                        ConnectionReuseStrategy reustrat,
                                                        ConnectionKeepAliveStrategy kastrat,
                                                        HttpRoutePlanner rouplan,
                                                        HttpProcessor httpProcessor,
                                                        HttpRequestRetryHandler retryHandler,
                                                        RedirectStrategy redirectStrategy,
                                                        AuthenticationStrategy targetAuthStrategy,
                                                        AuthenticationStrategy proxyAuthStrategy,
                                                        UserTokenHandler userTokenHandler,
                                                        HttpParams params)
  {
    return new RedirectRequestDirector(log,
                                       requestExec,
                                       conman,
                                       reustrat,
                                       kastrat,
                                       rouplan,
                                       httpProcessor,
                                       retryHandler,
                                       redirectStrategy,
                                       targetAuthStrategy,
                                       proxyAuthStrategy,
                                       userTokenHandler,
                                       params);
  }

}
