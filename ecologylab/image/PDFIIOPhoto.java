/**
 * 
 */
package ecologylab.image;

import java.awt.Color;
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

	public PDFIIOPhoto(ParsedURL purl, Dict imgdict, InputStream in, PDFReader pdfReader, DispatchTarget<PixelBased> dispatchTarget, GraphicsConfiguration graphicsConfiguration,
			Dimension maxDimension)
	{
		super(purl, dispatchTarget, graphicsConfiguration);
	}
	
	public BufferedImage createImage(Dict imgdict, InputStream in,
			Color fillcolor, PDFReader pdfr) throws IOException
	{
		assert imgdict != null
				&& in != null
				&& ("Image".equals(imgdict.get("Subtype")) || null == imgdict
						.get("Subtype")/* inline */);

		BufferedImage img	= null;
		int width		= pdfr.getObjInt(imgdict.get("Width"));
		int height	= pdfr.getObjInt(imgdict.get("Height"));

		String filter = Images.getFilter(imgdict, pdfr);
		if ("DCTDecode".equals(filter))
		{
//			img = createJPEG(imgdict, in, pdfr);
			ColorSpace cs = pdfr.getColorSpace(imgdict.get("ColorSpace"), null, null);
			int nComp 		= cs.getNumComponents();
			if (nComp == 4)
			{
				error("JPEG with 4 components not yet supported: " + cs);
			}
			else
			{	// 3 component JPEG
				
			}
		}
		else 
		{
			error("Filter not yet supported: " + filter);
			/*
			if ("JPXDecode".equals(filter))
				img = createJPEG2000(imgdict, in);
			else if ("CCITTFaxDecode".equals(filter))
				img = createFAX(imgdict, in, fillcolor, pdfr);
			else if ("JBIG2Decode".equals(filter))
				img = createJBIG2(imgdict, in);
			else
			{
				img = createRaw(imgdict, w, h, in, fillcolor, pdfr);
			} // raw samples, including most inline images
			*/
		}
		if (img == null)
			return null; // IOException, JBIG2, or problem with samples
			// long end = System.currentTimeMillis();
			// System.out.println("time = "+(end-start));
		// assert w==img.getWidth(): "width="+img.getWidth()+" vs param "+w; //
		// possible that parameters are wrong
		// X assert h==img.getHeight(): "height="+img.getHeight()+" vs param "+h;
		// => if short of data, shrink height

		return img;
	}

}
