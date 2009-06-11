/**
 * 
 */
package ecologylab.image;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import multivalent.std.adaptor.pdf.Dict;
import multivalent.std.adaptor.pdf.Images;
import multivalent.std.adaptor.pdf.PDFReader;
import ecologylab.generic.DispatchTarget;
import ecologylab.io.BasicSite;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.documentstructure.ImageConstants;
import ecologylab.semantics.html.documentstructure.ImageFeatures;

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

	public PDFIIOPhoto(ParsedURL purl, Dict imgXobj, InputStream in, PDFReader pdfReader, GraphicsConfiguration graphicsConfiguration,
			Dimension maxDimension)
	throws IOException
	{
		super(purl, null, graphicsConfiguration);
		this.maxDimension = maxDimension;

		Rendering rendering	= createImage(imgXobj, in, pdfReader);
		rendering = scaleRenderingUnderMaxDimension(rendering, rendering.width, rendering.height);

		initializeRenderings(rendering);
	}
	
	public Rendering createImage(Dict imgXobj, InputStream in, PDFReader pdfr) 
	throws IOException
	{
		String filter = Images.getFilter(imgXobj, pdfr);
		BufferedImage bImg	= null;
		if ("DCTDecode".equals(filter) || "JPEG2000".equals(filter))
		{
//			img = createJPEG(imgXobj, in, pdfr);
			ColorSpace cs = pdfr.getColorSpace(imgXobj.get("ColorSpace"), null, null);
			int nComp 		= cs.getNumComponents();
			if (nComp == 4)
			{
				bImg = Images.createImage(imgXobj, in, null, pdfr);			
//		  throw new IOException("JPEG with 4 components not yet supported: " + cs);
			}
			else
			{	// 3 component JPEG
				return this.imageIORead(in);
			}
		}
		else 
		{
//			if ("JPXDecode".equals(filter))
//				bImg = Images.createImage(imgXobj, in, null, pdfr);
//			else if ("CCITTFaxDecode".equals(filter))
//				bImg = Images.createImage(imgXobj, in, null, pdfr);
//			else if ("JBIG2Decode".equals(filter))
//				bImg = Images.createImage(imgXobj, in, null, pdfr);
//			else
//			{// raw samples, including most inline images
				bImg = Images.createImage(imgXobj, in, null, pdfr);
//			} 
		}
		if (bImg != null)
		{
			return new Rendering(this, bImg, null, null);

		}
		throw new IOException("Filter not yet supported: " + filter);
	}

}
