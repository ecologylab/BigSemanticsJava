package ecologylab.bigsemantics.downloaders.controllers;

import java.io.IOException;
import java.io.InputStream;
import ecologylab.net.ParsedURL;

public interface NewDownloadController
{
    public void setUserAgent(String userAgent);

    public boolean connect(ParsedURL location) throws IOException;

    public boolean isGood();

    public int getStatus();

    public String getStatusMessage();

    public ParsedURL getLocation();

    public ParsedURL getRedirectedLocation();

    public String getMimeType();

    public String getCharset();

    public String getHeader(String name);

    public InputStream getInputStream();
}
