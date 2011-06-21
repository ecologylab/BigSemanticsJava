/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
 * CONFIDENTIAL. Use is subject to license terms.
 */
package ecologylab.image;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

import ecologylab.collections.CollectionTools;
import ecologylab.generic.Continuation;
import ecologylab.io.Assets;
import ecologylab.io.AssetsRoot;
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

	public 	static final String 		INTERFACE			= "interface/";
	private static final AssetsRoot INTERFACE_ASSETS_ROOT = new AssetsRoot(INTERFACE, null);

	/**
	 * Most popular constructor.
	 * @param purl			ParsedURL to download image from.
	 * @param continuation	Call the delivery method on this object when
	 *			download is complete.
	 * @param graphicsConfiguration TODO
	 */
	public IIOPhoto(ParsedURL purl, Continuation<PixelBased> continuation, GraphicsConfiguration graphicsConfiguration)
	{
		this(purl, continuation, null, graphicsConfiguration, null);
	}

	/**
	 * Most popular constructor.
	 * @param purl			ParsedURL to download image from.
	 * @param continuation	Call the delivery method on this object when
	 *			download is complete.
	 * @param graphicsConfiguration TODO
	 */
	public IIOPhoto(ParsedURL purl, Continuation<PixelBased> continuation, BasicSite basicSite,
			GraphicsConfiguration graphicsConfiguration, Dimension maxDimension)
	{
		super(purl, continuation, basicSite, graphicsConfiguration, maxDimension);
	}

	/**
	 * @param base		base ParsedURL for forming the url to download image from.
	 * @param relativeURL	path relative to the base URL for forming the
	 *			url to download image from.
	 * @param continuation Call the delivery method on this object when
	 *			download is complete.
	 * @param graphicsConfiguration TODO
	 */
	public IIOPhoto(ParsedURL base, String relativeURL, Continuation<PixelBased> continuation, GraphicsConfiguration graphicsConfiguration)
	throws MalformedURLException
	{ 
		this(ParsedURL.getRelative(base.url(), relativeURL, ""), continuation, graphicsConfiguration);
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
			downloadMonitor.download(this, continuation);
		return result;
	}

	/////////////////////// methods for downloadable //////////////////////////

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
		File cachedFile 	= new File(INTERFACE_ASSETS_ROOT.getCacheRoot(), fileName);

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
	public void callback(PixelBased iioPhoto)
	{
		BufferedImage bufferedImage	= basisRendering.bufferedImage;
		if (bufferedImage != null)
			cacheImage(filename, basisRendering.bufferedImage);
	}

	public static PixelBased getCachedIIOPhoto(String imagePath, Continuation<PixelBased> continuation, GraphicsConfiguration graphicsConfiguration)
	{
		//FIXME need to make sure zip has been downloaded here
		// if not, initiate download & wait for it!

		File cachedInterfaceFile = Assets.getAsset(INTERFACE_ASSETS_ROOT, imagePath);
		ParsedURL cachedImagePURL	= new ParsedURL(cachedInterfaceFile);
		PixelBased result = new IIOPhoto(cachedImagePURL, continuation, graphicsConfiguration);
		//		result.downloadWithHighPriority();
		result.useHighPriorityDownloadMonitor();
		result.download();
		//		if (result.isDownloadDone())
		//			result.callback(dispatchTarget);
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

