/**
 * 
 */
package ecologylab.bigsemantics.downloaders.oodss;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import ecologylab.bigsemantics.filestorage.FileSystemStorage;
import ecologylab.collections.Scope;
import ecologylab.net.NetTools;
import ecologylab.oodss.distributed.server.DoubleThreadedNIOServer;
import ecologylab.serialization.SimplTypesScope;

/**
 * represents pool of machines for downloading document
 * 
 * @author ajit
 * 
 */
public class OODSSDownloaderInstance
{

  private static final String propertiesFile = "/downloader.properties";

  private static final int    idleTimeout    = -1;

  private static final int    MTU            = 10000000;

  private static final int    port           = 2107;

  private static void loadProperties() throws IOException
  {
    Properties props = new Properties();
    InputStream in = OODSSDownloaderInstance.class.getResourceAsStream(propertiesFile);
    props.load(in);
    in.close();

    FileSystemStorage.setDownloadDirectory(props);
  }

  public static void main(String[] args) throws IOException
  // public static void runInstance() throws IOException
  {
    loadProperties();
    
    SimplTypesScope lookupMetadataTranslations = SemanticsServiceDownloadMessageScope.get();

    /*
     * Creates a scope for the server to use as an application scope as well as individual client
     * session scopes.
     */
    Scope sessionScope = new Scope();

    /*
     * Initialize the ECHO_HISTORY registry in the application scope so that the performService(...)
     * of HistoryEchoRequest modifies the history in the application scope.
     */

    /* Acquire an array of all local ip-addresses */
    InetAddress[] locals = NetTools.getAllInetAddressesForLocalhost();

    /*
     * Create the server and start the server so that it can accept incoming connections.
     */
    DoubleThreadedNIOServer metadataServer =
        DoubleThreadedNIOServer.getInstance(port,
                                            locals,
                                            lookupMetadataTranslations,
                                            sessionScope,
                                            idleTimeout,
                                            MTU);
    metadataServer.start();
  }

}
