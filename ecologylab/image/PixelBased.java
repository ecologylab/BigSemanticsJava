package ecologylab.image;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.PackedColorModel;
import java.io.IOException;
import java.net.URL;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.ConsoleUtils;
import ecologylab.generic.Debug;
import ecologylab.generic.Continuation;
import ecologylab.io.BasicSite;
import ecologylab.io.Downloadable;
import ecologylab.net.ParsedURL;

/**
 * Infrastructure to display, keep track of, and manipulate pixel based media.
 * 
 * Works with {@link Rendering Rendering} to implement pipelined
 * image processing transformations on the bits.
 */
public class PixelBased
extends Debug
implements Downloadable
{
	///////////////////////////// image transform state ////////////////////////
	public static final int	NO_ALPHA_RADIUS		= -1;


	protected static final DirectColorModel	ARGB_MODEL	= new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0xff, 0xff000000);


	protected static final PackedColorModel	RGB_MODEL	= new DirectColorModel(24, 0xff0000, 0xff00, 0xff, 0);


	protected static final int[]	ARGB_MASKS	= { 0xff0000, 0xff00, 0xff, 0xff000000, };


	protected static final int[]	RGB_MASKS	= { 0xff0000, 0xff00, 0xff,  };


	static final int[]	RGB_BANDS	= { 0, 1, 2  };

	/**
	 * Net location of the whatever might get downloaded.
	 * Change from URL to ParsedURL.
	 */
	ParsedURL				purl;

	boolean					recycled;

	/**
	 * Don't run grab() more than once.
	 */
	boolean					grabbed;

	/**
	 * like unprocessedRendering, but never part of a chain.
	 * image buffer for the completely unaltered, the virgin.
	 * Usually unscaled, but may be scaled down due to maxDimension,
	 */
	Rendering				basisRendering;
	/**
	 * No image processing on this rendering, but if there is scaling to do, this one is scaled.
	 */
	protected Rendering		unprocessedRendering;
	AlphaGradientRendering	alphaGradientRendering;
	BlurredRendering			blurredRendering;
	DesaturatedRendering		desaturatedRendering;

	/**
	 * The current Rendering.
	 */
	Rendering				currentRendering;

	public final Object		renderingsLock	= new Object();
	/**
	 * First Rendering in the  pipeline (chain) that is dynamic,
	 * that is, that changes after 1st rendered.
	 */
	Rendering				firstDynamic;

	public Rendering getFirstDynamic()
	{
		return firstDynamic;
	}
	/**
	 * Size of the image, once we know it. Position, as well, if its on screen.
	 */
	protected Dimension		dimension 	= new Dimension();

	protected Dimension		originalDimension;

	//////////////////////// for debugging ////////////////////////////////

	boolean					scaled;

	public static int		constructedCount, recycledCount;


	/////////////////////// constructors //////////////////////////
	public PixelBased(BufferedImage bufferedImage)
	{
		this(bufferedImage, new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight()));
	}
	public PixelBased(BufferedImage bufferedImage, Dimension maxDimension)
	{
		Rendering rendering	= new Rendering(this, bufferedImage, null, null);

		rendering = scaleRenderingUnderMaxDimension(rendering, rendering.width, rendering.height, maxDimension);

		initializeRenderings(rendering);
 
		constructedCount++;
	}
	
	protected void initializeRenderings(Rendering rendering)
	{
		dimension					= new Dimension(rendering.width, rendering.height);
		basisRendering		= rendering;
		currentRendering	= rendering;
	}


	protected Rendering scaleRenderingUnderMaxDimension(Rendering rendering,
			int width, int height, Dimension maxDimension) 
	{
		if( maxDimension!= null )
		{
			int maxWidth	= maxDimension.width;
			int maxHeight	= maxDimension.height;
			if (width > maxWidth || height > maxHeight)
			{
				originalDimension	= new Dimension(width, height);
				if (width / (float) height < maxWidth / (float) maxHeight) 
				{
					width 	= (width*maxHeight + height/2) / height;
					height 	= maxHeight;
				} 
				else 
				{
					height 	= (height*maxWidth + width/2) / width;
					width 	= maxWidth;
				}
				Rendering origRendering	= rendering;
				if (width > 0 && height > 0)
				{ 
					rendering		= rendering.getScaledRendering(width, height);
				}
				origRendering.recycle();
				origRendering = null;	
			}
		}
		return rendering;
	}
//////////////////////from regular Image -> int[] pixels /////////////////////
	/**
	 * If possible, grab the image into memory. This is required for all
	 * image processing operations.
	 * This method creates the unprocessed <code>Rendering</code,
	 * if necessary. This will be true, for example, if this is based on an image stored with
	 * indexed color models.
	 *
	 * @return	true if the image can be grabbed; false if its an animated GIF
	 *		that we're not grabbing, or if the grab fails mysteriously.
	 */
	public boolean acquirePixelsIfNecessary()
	{
		// no need to fixPixels() more than once!
		if (grabbed)
			return true;
		grabbed	= true;

		basisRendering.fixPixels();

		return true;
	}

	//////////////////////// image processing ////////////////////////////////
	/**
	 * Scale the image to a new size, if possible and necessary;
	 *
	 * @param newWidth	new width for the image.
	 * @param newHeight	new height for the image.
	 * 
	 * @return	true if the operation succeeds (even if no new dimensions).
	 * @return	false if the image is timeBased, or bad.
	 */
	public boolean scaleInitially(int newWidth, int newHeight)
	{
//		debug("scale() " + dimension.width +","+ dimension.height+" -> " +
//		newWidth +","+ newHeight);
		if (recycled || (basisRendering == null))
			return false;

		if ((newWidth != dimension.width) || (newHeight != dimension.height))
		{
			if (unprocessedRendering == null)
			{
				synchronized (renderingsLock)
				{
					dimension.width				= newWidth;
					dimension.height			= newHeight;
					scaled								= true;
					basisRendering				= basisRendering.getScaledRendering(newWidth, newHeight);
					unprocessedRendering 	= new Rendering(basisRendering);
				}
			}
			else
			{
				//ConsoleUtils.obtrusiveConsoleOutput("Resize in scaleInitially()! " + this + " " + dimension+
				//		  " ->"+newWidth+","+newHeight);
				if ((this.alphaGradientRendering != null) && (this.unprocessedRendering.nextRendering != alphaGradientRendering))
				{
					ConsoleUtils.obtrusiveConsoleOutput("GASP! alphaGradient is not in the pipeline chain!");
				}
				this.resize(newWidth, newHeight);
				if ((this.alphaGradientRendering != null) && (alphaGradientRendering.width != dimension.width))
					ConsoleUtils.obtrusiveConsoleOutput("EARLY CRAZY! this="+dimension.width+" alphaGrad="+this.alphaGradientRendering.width);

			}
		}
		else
		{
			// create new Rendering using the same pixels, DataBufferInt, BufferedImage
			// this is efficient!
			//ConsoleUtils.obtrusiveConsoleOutput("No Scaling! " + this + " " + dimension);
			if (unprocessedRendering == null)
				unprocessedRendering	= new Rendering(basisRendering);
			//unprocessedRendering		= unscaledRendering.getScaledRendering(newWidth, newHeight);
		}

		this.setCurrentRendering(unprocessedRendering);
		return true;
	}
	public void dontScaleInitially()
	{
		if (!recycled && (basisRendering != null))
		{
			// create new Rendering using the same pixels, DataBufferInt, BufferedImage
			// this is efficient!
			if (unprocessedRendering == null)
				unprocessedRendering	= new Rendering(basisRendering);
			this.setCurrentRendering(unprocessedRendering);
		}
	}
	public boolean resize(int newWidth, int newHeight)
	{
		//       debug("scale() " + extent.width +","+ extent.height+" -> " +
		//	     newWidth +","+ newHeight);
		if (recycled || (unprocessedRendering == null))
			return false;

		if ((newWidth != dimension.width) || (newHeight != dimension.height))
		{
			synchronized (renderingsLock)
			{
				dimension.width	= newWidth;
				dimension.height= newHeight;
				scaled			= true;
				unprocessedRendering.resize(newWidth, newHeight, basisRendering);
			}
		}
		return true;
	}
	/**
	 * Builds an alpha gradient, or feathered mask.
	 * 
	 * Acts on buffer in the <code>pixels</code> slot.
	 * 
	 * @param	radius		area -- pixels of border to mask
	 * @param	minAlpha	lowest alpha setting in mask -- represented in
	 *				red, instead of alpha space, to avoid
	 *				signed arithmetic problems; will shift up later
	 */
	public boolean alphaGradient(int radius, int minAlpha)
	{
		if (recycled || (unprocessedRendering == null))
			return false;
		boolean active	= (radius != NO_ALPHA_RADIUS);

		if (alphaGradientRendering == null)
			alphaGradientRendering = new AlphaGradientRendering(unprocessedRendering, active);
		if (active)
		{
			alphaGradientRendering.compute(radius, minAlpha);
			Rendering last		= alphaGradientRendering.lastActive();
			if (last == alphaGradientRendering)
				//alphaGradientState.hookup();
				this.setCurrentRendering(alphaGradientRendering);
			else
				//	     last.compute();
				alphaGradientRendering.computeNext();
		}
		if (alphaGradientRendering.width != dimension.width)
			ConsoleUtils.obtrusiveConsoleOutput("CRAZY! this="+dimension.width+" alphaGrad="+this.alphaGradientRendering.width);
		return true;
	}
	/**
	 * @return In alpha gradient blending, the distance from the perimeter,
	 * inside which opacity is complete.
	 */
	public int alphaRadius()
	{
		return (alphaGradientRendering != null) ? alphaGradientRendering.radius : 0;
	}
	/**
	 * @return the minimum value opacity mask used for alpha gradient blending.
	 * [0, 255]
	 */
	public int minAlpha()
	{
		return (alphaGradientRendering != null) ? alphaGradientRendering.minAlpha : 0;
	}
	public boolean hasAlphaGradient()
	{
		return (alphaGradientRendering != null) && alphaGradientRendering.isActive;
	}
	public void blur2D(int blurWidth, int blurHeight, boolean immediate)
	{
		if (recycled || (unprocessedRendering == null))
			return;
		synchronized (renderingsLock)
		{
			if (blurredRendering == null)
			{
				if (desaturatedRendering == null)
					blurredRendering = new BlurredRendering(lastStatic(), true);
				else
					blurredRendering = new BlurredRendering(lastStatic(), desaturatedRendering, 
							true);
				firstDynamic = blurredRendering;
			}

			blurredRendering.compute(blurWidth, blurHeight, false);
			blurredRendering.goActive(immediate);

			/*	  blurState.goActive();
			  blurState.compute(blurWidth, blurHeight, immediate);
			  if (immediate && blurState.isLastActive())
			  blurState.hookup();
			 */
		}
	}
	public void noAlphaGradient()
	{
		this.setCurrentRendering(unprocessedRendering);
		unprocessedRendering.recycleRenderingChain(false);
		unprocessedRendering.nextRendering = null;
		alphaGradientRendering 	= null;
		desaturatedRendering 		= null;
		//goInactive(alphaGradientRendering);
	}
	public boolean restoreAlphaGradient()
	{
		if (alphaGradientRendering == null)
			return false;

		goActive(alphaGradientRendering, true);
		return true;
	}
	public void noBlur2D()
	{
		goInactive(blurredRendering);
	}
	public void blur2D(float degree, boolean immediate)
	{
		// for more perceptibly linear response, push degree toward 0
		//      degree		= MoreMath.bias(degree, .4f); 
		int blurWidth	= (int) ((float) dimension.width  * .25f * degree);
		int blurHeight	= (int) ((float) dimension.height * .25f * degree);
		blur2D(blurWidth, blurHeight, immediate);
	}
	/**
	 * @param degree	degree of desaturation
	 */
	public void desaturate(float	degree, boolean immediate)
	{
		if (show(1))
			debug("desaturate("+degree+") " + immediate);
		if (recycled || (unprocessedRendering == null))
			return;
		if (degree < .07f)
			return;

		Rendering blurredRendering	= this.blurredRendering;
		synchronized (renderingsLock)
		{
			if (desaturatedRendering == null)
			{
				if (blurredRendering == null)
				{
					desaturatedRendering
					= new DesaturatedRendering(lastStatic(), true);
					//		  = new DesaturateState(this, alphaGradientState, true);
					firstDynamic= desaturatedRendering;
				}
				else
				{
					desaturatedRendering
					= new DesaturatedRendering(blurredRendering, true);
				}
			}
			if ((blurredRendering != null) && (blurredRendering.previousRendering == null))
				throw new RuntimeException(this + " changed to NULL previousRendering?????);by new DesaturatedRendering");

			desaturatedRendering.compute(degree);
			//desaturatedRendering.goActive(immediate);
			setCurrentRendering(desaturatedRendering);
		} 
	}
	public void noDesaturate()
	{
		goInactive(desaturatedRendering);
	}


	/////////////////////// Rendering management //////////////////////////
	void goInactive(Rendering rendering)
	{
		if (!recycled && (rendering != null))
			rendering.goInactive();
	}
	void goActive(Rendering rendering, boolean immediate)
	{
		if (!recycled && (rendering != null))
			rendering.goActive(immediate);
	}
	boolean	useNoProc;
	public void setUseNoProc(boolean value)
	{
		useNoProc	= value;
	}

	/////////////////////// Images and MemoryImageSource //////////////////////////

	void setCurrentRendering(Rendering rendering)
	{
		if (!recycled)
			this.currentRendering	= rendering;
	}
////////////////////////sundry services ////////////////////////////////
	public Cursor createCustomCursor(Point point, String name)
	{
		Toolkit kit		= Toolkit.getDefaultToolkit();
		return kit.createCustomCursor(basisRendering.bufferedImage, point, name);
	}

////////////////////////rendering ////////////////////////////////

	Rendering lastStatic()
	{
		return (alphaGradientRendering == null) ? unprocessedRendering : alphaGradientRendering;
	}
	/**
	 * rendering
	 */
	public void paint(Graphics g, int x, int y)
	{
		if (!recycled)
		{
			Rendering rendering = useNoProc ? this.unprocessedRendering : this.currentRendering;
			if (rendering != null)
				rendering.paint(g, x, y);
			//else
			//debug("ERROR: trying to render, but no current rendering");
		}
	}
	public void paint(Graphics2D g2, int x, int y, AffineTransform a)
	{
		if (!recycled)
		{
			Rendering rendering = useNoProc ? this.unprocessedRendering : this.currentRendering;
			if (rendering != null)
				rendering.paint(g2, x, y, a);
			//else
			//debug("ERROR: trying to render, but no current rendering");
		}
	}

////////////////////////utilities ////////////////////////////////
	public String toString()
	{ 
		String addr = "["+ ((purl==null) ? "no purl - " + this.getClassName() : purl.toString())+"]";
		String dim  = (dimension == null) ? " " : ("[" + dimension.width+"x"+dimension.height + "] ");
		return getClassName(this) +  addr + dim;
	}
	public String errorMessage()
	{
		String purlString = (purl == null) ? "null" : purl.toString();
		return "** " + getClassName() + " can't access content: " + purlString;
	}

	public static String hex(int h)
	{
		return Integer.toHexString(h) + " ";
	}
	
	/**
	 * Encourage resource relamation -- flush <code>Image</code> and 
	 * release <code>Rendering</code> buffers..
	 */
	public void recycle()
	{
		synchronized (renderingsLock)
		{
			if (!recycled)
			{
				//debug("recycle()");
				recycled			= true;

				purl				= null;

				dimension		= null;
				
				if (unprocessedRendering != null)
				{
					unprocessedRendering.recycleRenderingChain(true);
					unprocessedRendering.recycle(); // also calls chain of Renderings
					unprocessedRendering		= null;
				}

				if (currentRendering != null)
				{
					currentRendering.recycle();
					currentRendering	= null;
				}
				
				if (firstDynamic != null)
				{
					firstDynamic.recycle();
					firstDynamic		= null;
				}
				
				if (basisRendering != null)
				{
					basisRendering.recycle();
					basisRendering	= null;
				}

				if (this.alphaGradientRendering != null)
				{
					alphaGradientRendering.recycle();
					alphaGradientRendering	= null;
				}
				if (this.desaturatedRendering != null)
				{
					desaturatedRendering.recycle();
					desaturatedRendering	= null;
				}
				if (this.blurredRendering != null)
				{
					blurredRendering.recycle();
					blurredRendering	= null;
				}
				recycledCount++;
			}
		}
	}

	/**
	 * Debugging info about the status of the image.
	 */   
	public String downScaled()
	{
		String scale= scaled ? " scaled" : "";
		return scale;
	}

	/**
	 * A shorter string for displaing in the modeline for debugging, and
   in popup messages.
	 */
	public static String shortURL(URL u)
	{
		String s;
		if (u == null)
			s	= "Img null";
		else
		{
			String file	= u.getFile();
			s	= u.getHost() + "/.../" + 
			file.substring(file.lastIndexOf('/') + 1);
		}
		return s;
	}
	public String shortURL()
	{
		return shortURL(purl.url());
	}

	/* return ParsedURL */
	public ParsedURL location()
	{
		return purl;
	}

	/**
	 * Change alpha values throughout the pixels in the currentRendering.
	 * Multiply existing alpha by the value passed in.
	 * This means that transparent areas stay transparent (newAlpha * 0),
	 * while opaque areas get this level of transparency (newAlpha * 0xff).
	 * @param scaleFactor TODO
	 */
	public void scaleAlpha(float scaleFactor)
	{
		currentRendering.scaleAlpha(scaleFactor);
	}

	public int getWidth()
	{
		return (dimension != null) ? dimension.width : 0;
	}

	public int getHeight()
	{
		return (dimension != null) ? dimension.height : 0;
	}

	public Dimension getDimension()
	{
		return dimension;
	}

	/**
	 * If scaling was performed, the dimensions of the image associated with this *before hand*.
	 * 
	 * @return	The original dimensions of the image, before scaling, or null, if no scaling was performed.
	 */
	public Dimension getOriginalDimesion()
	{
		return originalDimension;
	}
	public void resetOriginalDimesion()
	{
		originalDimension = null;
	}

	public boolean timedOut()
	{
		return false;
	}
	public boolean shouldDownloadBiggerVersion() {
		/* if originalDimension exists, we scaled the picture down when first downloading it.
		 so if the user wants to make it bigger than the basisRendering, 
		 we should re-download it and not scale it down */
		return (/* originalDimension != null && */
				dimension.width > basisRendering.width ||
				dimension.height > basisRendering.height);
	}
	
	public BufferedImage bufferedImage()
	{
		return (currentRendering != null) ? currentRendering.bufferedImage : null;
	}

	/**
	 * Do nothing, but implement Downloadable.
	 */
	public void handleIoError()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Do nothing, but implement Downloadable.
	 */
	public boolean isRecycled()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Do nothing, but implement Downloadable.
	 */
	public void performDownload() throws IOException
	{
		// TODO Auto-generated method stub
		
	}

	public BasicSite getSite()
	{
		return null;
	}
	
  /**
   * 
   * @return	What to tell the user about what is being downloaded.
   */
  public String message()
  {
  	return "image " + purl;
  }
}
