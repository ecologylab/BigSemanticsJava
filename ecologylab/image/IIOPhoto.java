/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
 * CONFIDENTIAL. Use is subject to license terms.
 */
package ecologylab.image;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

import ecologylab.collections.CollectionTools;
import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.DispatchTarget;
import ecologylab.io.Assets;
import ecologylab.io.BasicSite;
import ecologylab.io.Downloadable;
import ecologylab.net.ConnectionAdapter;
import ecologylab.net.PURLConnection;
import ecologylab.net.ParsedURL;

/**
 * Photo integrates Image with a set of operations on (bitmapped)images, so
 * that these operations are conveniently invoked;
 * operations include download, download and repaint, download and get
 * called back, scale. 
 * Automatic repainting of animated gifs is also supported.
 * <p>
 * 
 * Robustly promises a download() callback, even if the image cant be gotten,
 * in which case the bad flag will be set (implements timeout).
 * <p>
 *
 * Part of the Playlets alternative window system.
 * <p>
 * 
 * This version encapsulates the (straightforward) Java 1.4 
 * javax.imagio.ImageIO object, instead of the (convoluted) Java 1.0 
 * java.awt.Image object. It uses
 * {@link ecologylab.concurrent.DownloadMonitor DownloadMonitor} to do the actual
 * downloading.
 * Instead of rigid oo encapsulation (with get and set methods for all
 * access of state variables), a cooperative inter-module approach is adopted.
 * Specifically, to minimize download sizes, many state variables are
 * declared as public. This DOES NOT mean its reasonable to set them
 * from outside at any time.
 * <p>
 * 
 * Works with {@link Rendering ImageState} to implement pipelined
 * image processing transformations on the bits.
 */
public class IIOPhoto
extends PixelBased
implements Downloadable, DispatchTarget<IIOPhoto>
{
	PURLConnection	purlConnection;

	ImageInputStream	imageInputStream;
	ImageReader		imageReader;

	boolean			timedOut;
	String			filename = null;

	static String[]	NO_ALPHA_SUFFIXES			= 
	{
		"BMP", "bmp", "JPEG", "jpeg", "JPG", "jpeg", "WBMP", "wbmp",  
	};
	static HashMap<String, String>	noAlphaBySuffixMap	= CollectionTools.buildHashMapFromStrings(NO_ALPHA_SUFFIXES);



	/**
	 * Most popular constructor.
	 * @param purl			ParsedURL to download image from.
	 * @param dispatchTarget	Call the delivery method on this object when
	 *			download is complete.
	 * @param graphicsConfiguration TODO
	 */
	public IIOPhoto(ParsedURL purl, DispatchTarget<PixelBased> dispatchTarget, GraphicsConfiguration graphicsConfiguration)
	{
		this(purl, dispatchTarget, null, graphicsConfiguration, null);
	}

	/**
	 * Most popular constructor.
	 * @param purl			ParsedURL to download image from.
	 * @param dispatchTarget	Call the delivery method on this object when
	 *			download is complete.
	 * @param graphicsConfiguration TODO
	 */
	public IIOPhoto(ParsedURL purl, DispatchTarget<PixelBased> dispatchTarget, BasicSite basicSite,
			GraphicsConfiguration graphicsConfiguration, Dimension maxDimension)
	{
		super(purl, dispatchTarget, basicSite, graphicsConfiguration, maxDimension);
	}

	/**
	 * @param base		base ParsedURL for forming the url to download image from.
	 * @param relativeURL	path relative to the base URL for forming the
	 *			url to download image from.
	 * @param dispatchTarget Call the delivery method on this object when
	 *			download is complete.
	 * @param graphicsConfiguration TODO
	 */
	public IIOPhoto(ParsedURL base, String relativeURL, DispatchTarget<PixelBased> dispatchTarget, GraphicsConfiguration graphicsConfiguration)
	throws MalformedURLException
	{ 
		this(ParsedURL.getRelative(base.url(), relativeURL, ""), dispatchTarget, graphicsConfiguration);
	}
	public IIOPhoto(ParsedURL purl, PURLConnection purlConnection,
			DispatchTarget<PixelBased> dispatchTarget, GraphicsConfiguration graphicsConfiguration, Dimension maxDimension)
	{
		super(purl, dispatchTarget, graphicsConfiguration, maxDimension);

		this.purlConnection		= purlConnection;
		imageIORead(purlConnection);
	}
	/**
	 * For outside callers to initiate download of the media.
	 * Contrasts with performDownload(), which in some cases, happens later,
.* in a DownloadMonitor thread.
	 * 
	 * @return false -- if the download is already in progress, and doesnt
	 * need to be executed.
	 */
	public boolean download()
	{
		boolean result	= !downloadStarted;
		if (result)
			downloadMonitor.download(this, dispatchTarget);
		return result;
	}

	/////////////////////// methods for downloadable //////////////////////////
	/**
	 * Called by the DowloadMonitor to request Image "download". The Image
	 * may already be here 
	 *(in cache), or may require net transfer.
	 * 
	 * Results in a call of DispatchTarget.delivery() sooner or later, hopefully
	 * in a new Thread.
	 * Also in a notify(), if someone's in waitForReady();
	 */
	public void performDownload()
	{
		if (!downloadStarted)
		{
			downloadStarted	= true;
			PURLConnection purlConnection	= getPURLConnection();
			if (purlConnection != null)
			{
				Rendering initialState	= imageIORead(purlConnection);
				if (initialState == null)
				{
					return; //Normal pathway for errors during image read
				}
				if (dimension == null)
				{
					debug("performDownload() dimension == NULL!!!??");
					dimension	= new Dimension();
				}
				basisRendering		= initialState;
				currentRendering	= initialState;
				dimension.width		= initialState.width;
				dimension.height	= initialState.height;
				downloadDone		= true;
			}
			else
			{
				// recycle resources when the timeout happens.
				if( purl.getTimeout() )
					handleTimeout();

				error("Can't connect.");
			}
		}
	}

	static final DirectColorModel ARGB_MODEL	= new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0xff, 0xff000000);

	//static final DirectColorModel RGB_MODEL	= new DirectColorModel(32, 0xff0000, 0x00ff00, 0xff);
	static final PackedColorModel RGB_MODEL	= new DirectColorModel(24, 0xff0000, 0xff00, 0xff, 0);

	static final int[] ARGB_MASKS			= { 0xff0000, 0xff00, 0xff, 0xff000000, };
	static final int[] RGB_MASKS				= { 0xff0000, 0xff00, 0xff,  };
	static final int[] RGB_BANDS				= { 0, 1, 2  };

	/**
	 * Obtain a PURLConnection from the purl, unless we already have one,
	 * in which case do nothing.
	 * Sets the purlConnection instance variable, as well as returning a reference.
	 * 
	 * @return
	 * @throws IOException
	 */
	PURLConnection getPURLConnection()
	{
		// PURLConnection may be preset, in which case no need to obtain it again!
		if (purlConnection == null)
			purlConnection	= purl.connect(new ConnectionAdapter()
			{
				public boolean parseFilesWithSuffix(String suffix)
				{
					return ParsedURL.isImageSuffix(suffix);
				}
			});
		return purlConnection;
	}

	public static final int MIN_DIM	= 10;

	protected Rendering imageIORead(PURLConnection purlConnection)
	{
		Rendering		result					= null;
		BufferedImage	bufferedImage			= null;

		InputStream		inputStream				= purlConnection.inputStream();
		try
		{
			imageInputStream	= ImageIO.createImageInputStream(inputStream);
			if ((imageInputStream == null) || timedOut)
				error("Cant open ImageInputStream for " + timedOut+" "+purl);
			else
			{
				Iterator<ImageReader> imageReadersIterator		= ImageIO.getImageReaders(imageInputStream);
				if (!imageReadersIterator.hasNext() || timedOut)
					error("Cant get reader for " +timedOut+" "+ purl);
				else
				{
					imageReader	= (ImageReader) imageReadersIterator.next();
					String formatName	= imageReader.getFormatName();
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
						else if (purl.isNoAlpha())
						{
							readImageType	= BufferedImage.TYPE_INT_RGB;
						}
						// catch server-side content mime type setting for images from a repository
						else if (noAlphaBySuffixMap.containsKey(formatName))
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
							error("Cant read from imageReader for " + purl);
						else
						{
							result				= new Rendering(this, bufferedImage, dataBuffer, pixels);
							result				= scaleRenderingUnderMaxDimension(result, width, height);
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
		} catch (SocketTimeoutException e)
		{
			bad 			= true;
			timedOut 		= true;
			handleTimeout();
			error("imageIORead() " + e);
		} catch (Exception e)
		{
			bad 			= true;
			error("ERROR caught while reading image: " + e);
//			if (e instanceof NullPointerException)
				e.printStackTrace();
			recycle();
		}
		finally
		{
			freeImageIOResources();
		}

		return (timedOut || bad) ? null : result;
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
		if (purlConnection != null)
		{
			purlConnection.recycle();
			purlConnection	= null;
		}
		return result;
	}

	/**
	 * @return false when the thing could not be stopped and a new thread started.
	 */
	public synchronized boolean handleTimeout()
	{
		if (!downloadDone)
		{
			if (!timedOut)
				timedOut				= true;
			else
				weird("handleTimeout() handleTimeout() called again");

			bad						= true;
			if (imageReader != null)
				imageReader.abort();

			freeImageIOResources();
			super.error("TIMEOUT while downloading image.");
			recycle();
		}
		return true;
	}
	public void handleIoError()
	{
		//FIXME shouldn't this call handleTimeout() to free resources
		//FIXME should this call delivery()???!
		error("IOERROR");
	}

	/**
	 * 
	 * @return	true if a timeout error occurred during download.
	 */
	@Override
	public boolean timedOut()
	{
		return timedOut;
	}

	/**
	 * Tells the downloaded image to write itself to disk. Used for caching photos.
	 * 
	 * Note: since this is public, the cache location cannot be changed.
	 */

	/**
	 * Cache an image in memory to the Asset's cache.
	 * 
	 * @param fileName Name of the file to save to disk
	 * @param pixels	integer representation of the image
	 * @return true if the cache save was successful, false otherwise.
	 */
	protected boolean cacheImage(String fileName, BufferedImage outputImage)
	{
		//if the downloaded isn't complete, monitor it and cache when finished
		if (!isDownloadDone() || outputImage == null)
		{
			downloadMonitor.download(this, this);
			this.filename = fileName;
			return false;
		}

		/* 
		 * The download must be finished, so cache the file.
		 */
		//the cache location
		File cachedFile 	= new File(Assets.getAsset(Assets.INTERFACE), fileName);

		//actually write to disk
		try 
		{ 
			//check two levels of directories to ensure they exist 
			if (!cachedFile.getParentFile().exists())
				cachedFile.getParentFile().mkdirs();


			ImageIO.write(outputImage, "png", cachedFile); //write the png
			System.out.println("Cached PNG: " + cachedFile);
		}
		catch (Exception e)
		{
			System.out.println("cacheImage: Exception: " + e);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Notify us when a cachable image has been downloaded and should
	 * then be cached to disk.
	 */
	public void delivery(IIOPhoto o)
	{
		BufferedImage bufferedImage	= basisRendering.bufferedImage;
		if (bufferedImage != null)
			cacheImage(filename, basisRendering.bufferedImage);
	}

	public static IIOPhoto getCachedIIOPhoto(String imagePath, DispatchTarget<PixelBased> dispatchTarget, GraphicsConfiguration graphicsConfiguration)
	{
		//FIXME need to make sure zip has been downloaded here
		// if not, initiate download & wait for it!
		ParsedURL cachedImagePURL	= new ParsedURL(Assets.getCachedInterfaceFile(imagePath));
		IIOPhoto result = new IIOPhoto(cachedImagePURL, dispatchTarget, graphicsConfiguration);
		//		result.downloadWithHighPriority();
		result.useHighPriorityDownloadMonitor();
		result.download();
		//		if (result.isDownloadDone())
		//			result.delivery(dispatchTarget);
		return result;
	}

	/**
	 * True if the Downloadable has been recycled, and thus should not be downloaded.
	 * 
	 * @return
	 */
	public boolean isRecycled()
	{
		return recycled;
	}

}

