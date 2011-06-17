/**
 * 
 */
package ecologylab.semantics.documentparsers;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PackedColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.builtins.Image;

/**
 * @author andruid
 *
 */
public class ImageParserAwt extends DocumentParser<Image>
{
	ImageInputStream	imageInputStream;
	ImageReader				imageReader;

	
	static final DirectColorModel ARGB_MODEL	= new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0xff, 0xff000000);

	//static final DirectColorModel RGB_MODEL	= new DirectColorModel(32, 0xff0000, 0x00ff00, 0xff);
	static final PackedColorModel RGB_MODEL	= new DirectColorModel(24, 0xff0000, 0xff00, 0xff, 0);

	static final int[] ARGB_MASKS			= { 0xff0000, 0xff00, 0xff, 0xff000000, };
	static final int[] RGB_MASKS				= { 0xff0000, 0xff00, 0xff,  };
	static final int[] RGB_BANDS				= { 0, 1, 2  };

	private static boolean				inited;

	public static void init()
	{
		if (!inited)
		{
			inited		= true;
			DocumentParser.init();
			bindingParserMap.put(SemanticActionsKeyWords.IMAGE_PARSER, ImageParserAwt.class);
		}
	}
	/**
	 * 
	 */
	public ImageParserAwt()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param infoCollector
	 */
	public ImageParserAwt(NewInfoCollector infoCollector)
	{
		super(infoCollector);
	}

/**
 * 
 * @see ecologylab.semantics.documentparsers.DocumentParser#parse()
 */
	@Override
	public void parse() throws IOException
	{
		getDocument().setParserResult(new ImageParserAwtResult(imageIORead(inputStream())));
	}

	public static final int MIN_DIM	= 10;

	protected BufferedImage imageIORead(InputStream		inputStream) throws IOException
	{
		BufferedImage	bufferedImage		= null;

		ParsedURL location					= getDocument().getLocation();
		imageInputStream	= ImageIO.createImageInputStream(inputStream);
		if (imageInputStream == null)
			error("Cant open ImageInputStream for " + location);
		else
		{
			Iterator<ImageReader> imageReadersIterator		= ImageIO.getImageReaders(imageInputStream);
			if (!imageReadersIterator.hasNext())
				error("Cant get reader for " + location);
			else
			{
				imageReader	= (ImageReader) imageReadersIterator.next();
//				String formatName			= imageReader.getFormatName();
				imageReader.setInput(imageInputStream, true, true);
				
				ImageReadParam param	= imageReader.getDefaultReadParam();
				int width		= imageReader.getWidth(0);
				int height	= imageReader.getHeight(0);

				if ((width > MIN_DIM) && (height > MIN_DIM))
				{
					// try to setup the BufferedImage we use for the read to be structured
					// the way we like the data -- as an array of int[].
					// this means trying to find out about the image's structure,
					// in particular, if its rgb -- 3 color "bands", or argb alread -- 4 bands
					int readImageType	= -1;
					//TODO this line is creating byte arrays :-(
					ImageTypeSpecifier rawImageType	= imageReader.getRawImageType(0);
					// try to find out directly from ImageIO about the file's header
					if (rawImageType != null) // unfortunately this doesnt seem to work much for URLConnection images
					{
						int rawNumBands	= rawImageType.getNumBands();
						switch (rawNumBands)
						{
						case 4:
							readImageType= BufferedImage.TYPE_INT_ARGB;
							break;
						case 3:
							readImageType= BufferedImage.TYPE_INT_RGB;
							break;
						default:
							if (rawImageType.getColorModel() instanceof IndexColorModel)
							{
								//									readImageType= BufferedImage.TYPE_BYTE_INDEXED;
							}
							break;
						}
						//debug("gotRawImageType! numBands="+rawNumBands+ " readImageType="+readImageType);
					}
					// look in the URL itself
					else if (location.isNoAlpha() || purlConnection.isNoAlpha())
					{
						readImageType	= BufferedImage.TYPE_INT_RGB;
					}
					int[] 			pixels			= null;
					DataBufferInt	dataBuffer		= null;
					if (readImageType != -1)
					{
						ColorModel	cm;
						int[]		masks;
						if (readImageType == BufferedImage.TYPE_INT_RGB)
						{
							cm		= RGB_MODEL;
							masks	= RGB_MASKS;
							//param.setDestinationBands(RGB_BANDS);
						}
						else
						{
							cm		= ARGB_MODEL;
							masks	= ARGB_MASKS;
						}
						SampleModel		sm		= new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width, height, masks);
						int numPixels				= width*height;
						pixels							= new int[numPixels];
						dataBuffer					= new DataBufferInt(pixels, numPixels);
						WritableRaster	wr	= Raster.createWritableRaster(sm, dataBuffer, null);
						bufferedImage				= new BufferedImage(cm, wr, false, null);
						/*
						 if (graphicsConfiguration != null)
						 {
						 int transparency = (readImageType == BufferedImage.TYPE_INT_ARGB) ?
						 Transparency.TRANSLUCENT : Transparency.OPAQUE;
						 bufferedImage =
						 graphicsConfiguration.createCompatibleImage(width, height, transparency);
						 }
						 else
						 bufferedImage		= new BufferedImage(width, height, readImageType);
						 */
						param.setDestination(bufferedImage);
					}
					// read, using the BufferedImage we made, or the default
					// if we set result in the line above, we'll just get it back, so no problem.
					bufferedImage			= imageReader.read(0, param);
					if (readImageType == -1)
					{

					}

					if (bufferedImage == null)
					{
						throw new IOException("ImageParserAwt Cant read from imageReader for " + location);
					}
					while (imageReadersIterator.hasNext())
					{
						ImageReader nextReader	= (ImageReader) imageReadersIterator.next();
						debug("COOL: Freeing resources on additional readers! " + nextReader);
						nextReader.reset();
						nextReader.dispose();
					}
				}
				// desparate attempts to reduce referentiality :-)
				param.setDestination(null);
				param.setSourceBands(null);
				param.setDestinationBands(null);
				param.setDestinationType(null);
				param.setController(null);
			}
		}
		freeImageIOResources();

		return bufferedImage;
	}
	
	@Override
	public synchronized void recycle()
	{
		freeImageIOResources();
		super.recycle();
	}

	private boolean freeImageIOResources()
	{
		boolean result			= false;
		if (imageReader != null)
		{
			imageReader.reset();  // release all resources and set to initial state
			imageReader.dispose();
			imageReader			= null;
			result				= true;
		}
		if (imageInputStream != null)
		{
			try
			{
				imageInputStream.flush();
				imageInputStream.close();
				imageInputStream= null;
				result			= true;
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			this.imageInputStream= null;
		}
		return result;
	}

	/**
	 * @return false when the thing could not be stopped and a new thread started.
	 */
	public synchronized void handleIoError()
	{
		if (imageReader != null)
			imageReader.abort();

		freeImageIOResources();
		super.error("TIMEOUT while downloading image.");
		recycle();
	}
	
	@Override
	public DownloadMonitor<DocumentClosure> downloadMonitor(boolean isDnd, boolean isSeed)
	{
		return  isDnd ? NewInfoCollector.IMAGE_DND_DOWNLOAD_MONITOR : NewInfoCollector.IMAGE_DOWNLOAD_MONITOR;
	}

}
