/**
 * 
 */
package ecologylab.image;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.net.MalformedURLException;

import ecologylab.generic.DispatchTarget;
import ecologylab.io.BasicSite;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;

/**
 * @author andruid
 *
 */
public class PDFIIOPhoto extends IIOPhoto
{

	/**
	 * @param purl
	 * @param dispatchTarget
	 * @param graphicsConfiguration
	 */
	public PDFIIOPhoto(ParsedURL purl, DispatchTarget<PixelBased> dispatchTarget,
			GraphicsConfiguration graphicsConfiguration)
	{
		super(purl, dispatchTarget, graphicsConfiguration);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param purl
	 * @param dispatchTarget
	 * @param basicSite
	 * @param graphicsConfiguration
	 * @param maxDimension
	 */
	public PDFIIOPhoto(ParsedURL purl, DispatchTarget<PixelBased> dispatchTarget,
			BasicSite basicSite, GraphicsConfiguration graphicsConfiguration, Dimension maxDimension)
	{
		super(purl, dispatchTarget, basicSite, graphicsConfiguration, maxDimension);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param base
	 * @param relativeURL
	 * @param dispatchTarget
	 * @param graphicsConfiguration
	 * @throws MalformedURLException
	 */
	public PDFIIOPhoto(ParsedURL base, String relativeURL, DispatchTarget<PixelBased> dispatchTarget,
			GraphicsConfiguration graphicsConfiguration) throws MalformedURLException
	{
		super(base, relativeURL, dispatchTarget, graphicsConfiguration);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param purl
	 * @param purlConnection
	 * @param dispatchTarget
	 * @param graphicsConfiguration
	 * @param maxDimension
	 */
	public PDFIIOPhoto(ParsedURL purl, PURLConnection purlConnection,
			DispatchTarget<PixelBased> dispatchTarget, GraphicsConfiguration graphicsConfiguration,
			Dimension maxDimension)
	{
		super(purl, purlConnection, dispatchTarget, graphicsConfiguration, maxDimension);
		// TODO Auto-generated constructor stub
	}

	
}
