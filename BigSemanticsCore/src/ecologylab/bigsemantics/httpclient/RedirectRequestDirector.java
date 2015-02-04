package ecologylab.bigsemantics.httpclient;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.impl.client.RoutedRequest;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

/**
 * Handling redirects.
 * 
 * @author quyin
 */
class RedirectRequestDirector extends DefaultRequestDirector
{

  public RedirectRequestDirector(Log log,
                                 HttpRequestExecutor requestExec,
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
    super(log,
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

  @Override
  protected RoutedRequest handleResponse(RoutedRequest roureq,
                                         HttpResponse response,
                                         HttpContext context) throws HttpException, IOException
  {
    RoutedRequest req = super.handleResponse(roureq, response, context);
    if (req != null)
    {
      String redirectTarget = req.getRoute().getTargetHost().getHostName();
      req.getRequest().getOriginal().setHeader("Host", redirectTarget);
    }
    return req;
  }

}
