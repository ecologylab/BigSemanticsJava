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
import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NodeList;

import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.GpsDirectory;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.collecting.SemanticsSessionScope;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.Image;
import ecologylab.semantics.sensing.GisFeatures;
import ecologylab.semantics.sensing.MetadataExifFeature;
import ecologylab.serialization.types.ImageAwtTypes;

/**
 * @author andruid
 *
 */
public class ImageParserAwt extends DocumentParser<Image>
{
	private static final String	MM_TAG_GPS_LOCATION	= "gps_location";
	private static final String	MM_TAG_CAMERA_SETTINGS	= "camera_settings";
	private static final int	EXIF_TAG_ORIGINAL_DATE	= 0x9003;
	ImageInputStream	imageInputStream;
	ImageReader				imageReader;

	static Class cameraClass, gpsClass;
	
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
			new ImageAwtTypes();
			
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
	public ImageParserAwt(SemanticsSessionScope infoCollector)
	{
		super(infoCollector);
		cameraClass	= infoCollector.getMetadataTranslationScope().getClassByTag(MM_TAG_CAMERA_SETTINGS);
		gpsClass		= infoCollector.getMetadataTranslationScope().getClassByTag(MM_TAG_GPS_LOCATION);
	}

/**
 * 
 * @see ecologylab.semantics.documentparsers.DocumentParser#parse()
 */
	@Override
	public void parse() throws IOException
	{
		Image image = getDocument();
		BufferedImage bufferedImage = imageIORead(inputStream());
		if (bufferedImage != null)
		{
			image.setParserResult(new ImageParserAwtResult(bufferedImage));
			image.setWidth(bufferedImage.getWidth());
			image.setHeight(bufferedImage.getHeight());
		}
		else
			image.error("ImageParserAwt failed for " + image.getLocation());
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
				String formatName			= imageReader.getFormatName();
				if ("JPEG".equals(formatName))
					readMetadata(false);
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
	
	private void readMetadata(boolean reread) throws IOException
	{
		try
		{
			IIOMetadata metadata	= imageReader.getImageMetadata(0);
	
			String name						= metadata.getNativeMetadataFormatName();
			IIOMetadataNode node	=(IIOMetadataNode) metadata.getAsTree(name);
	//		printTree(node);
			extractMetadataFeatures(node);
		} catch (javax.imageio.IIOException iioex)
		{
			// Crazy good workaround for java.imageio bug, from http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4924909
			if (!reread && iioex.getMessage() != null &&
          iioex.getMessage().endsWith("without prior JFIF!"))
			{
				warning("Trying workaround for java bug");
				closeImageInputStream();
				InputStream newInputSteam	= reConnect();
				imageInputStream = patch(newInputSteam);
				imageReader.setInput(imageInputStream);
				readMetadata(true);
//				IIOImage newImage = imageReader.readAll(0, null);
       }
			else
				warning("Couldn't extract metadata from image: " + iioex);
		}
//		byte[] iptc						=(byte[]) iptcNode.getUserObject();
	}
	
	private static ImageInputStream patch(InputStream in) throws IOException
	{
		in = new BufferedInputStream(in);
		in = new PatchInputStream(in);
		return ImageIO.createImageInputStream(in);
	}
  /** Patches a JPEG file that is missing a JFIF marker **/
	private static class PatchInputStream extends FilterInputStream
	{
		private static final int[]	JFIF			=
																					{ 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00,
																							0x01, 0x02, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00 };
		int													position	= 0;

		public PatchInputStream(InputStream in)
		{
			super(in);
		}

		public int read() throws IOException
		{
			int result;
			if (position < 2)
			{
				result = in.read();
			}
			else if (position < 2 + JFIF.length)
			{
				result = JFIF[position - 2];
			}
			else
			{
				result = in.read();
			}
			position++;
			return result;
		}

		public int read(byte[] b, int off, int len) throws IOException
		{
			final int max = off + len;
			int bytesread = 0;
			for (int i = off; i < max; i++)
			{
				final int bi = read();
				if (bi == -1)
				{
					if (bytesread == 0)
					{
						bytesread = -1;
					}
					break;
				}
				else
				{
					b[i] = (byte) bi;
					bytesread++;
				}
			}
			return bytesread;
		}
	}

	public static String	EXIF_ELEMENT_TAG_NAME	= "unknown";
	/**
	 * @param node
	 * @param exifTag
	 */
	public void extractMetadataFeatures(IIOMetadataNode node)
	{
		NodeList unknownElements			= node.getElementsByTagName(EXIF_ELEMENT_TAG_NAME);
		for (int i=0; i<unknownElements.getLength(); i++)
		{
			IIOMetadataNode foundUnknownNode			= (IIOMetadataNode) unknownElements.item(i);
			if ("225".equals(foundUnknownNode.getAttribute("MarkerTag")))
			{
				boolean dated 			= false;
				byte[] 	exifSegment	= (byte[]) foundUnknownNode.getUserObject();

				final com.drew.metadata.Metadata exifMetadata = new com.drew.metadata.Metadata();
				new ExifReader(exifSegment).extract(exifMetadata);
				com.drew.metadata.Directory exifDir = exifMetadata.getDirectory(ExifDirectory.class);

				Image image = getDocument();
				boolean mixedIn			= image.containsMixin(MM_TAG_CAMERA_SETTINGS);
				if (!mixedIn && !GisFeatures.containsGisMixin(image))
				{
					if (!dated && ORIG_DATE_FEATURE.extract(image, exifDir) == null)
					{
						dated		= true;
						DATE_FEATURE.extract(image, exifDir);
					}
					if (!mixedIn && CAMERA_MODEL_FEATURE.getStringValue(exifDir) != null)
					{
						mixedIn	= true;
						extractMixin(exifDir, EXIF_METADATA_FEATURES, MM_TAG_CAMERA_SETTINGS);
					}
					com.drew.metadata.Directory gpsDir = exifMetadata.getDirectory(GpsDirectory.class);
					Metadata gpsMixin	= GisFeatures.extractMixin(gpsDir, semanticsScope, image);
					Iterator<com.drew.metadata.Tag> gpsList = printDirectory(gpsDir);
					int qq = 33;
				}
			}
		}
	}
	static final MetadataExifFeature	DATE_FEATURE	= new MetadataExifFeature("creation_date", ExifDirectory.TAG_DATETIME);
	static final MetadataExifFeature	ORIG_DATE_FEATURE	= new MetadataExifFeature("creation_date", ExifDirectory.TAG_DATETIME_ORIGINAL);
	
	static final MetadataExifFeature	CAMERA_MODEL_FEATURE	= new MetadataExifFeature("model", ExifDirectory.TAG_MODEL);

	// // http://www.awaresystems.be/imaging/tiff/tifftags/privateifd/exif.html
	public static final MetadataExifFeature EXIF_METADATA_FEATURES[]	=
	{
		CAMERA_MODEL_FEATURE,
		new MetadataExifFeature("orientation", ExifDirectory.TAG_ORIENTATION),
		new MetadataExifFeature("resolution", ExifDirectory.TAG_X_RESOLUTION),
		new MetadataExifFeature("exposure_time", ExifDirectory.TAG_EXPOSURE_TIME),
		new MetadataExifFeature("aperture", ExifDirectory.TAG_APERTURE),
		new MetadataExifFeature("shutter_speed", ExifDirectory.TAG_SHUTTER_SPEED),
		new MetadataExifFeature("subject_distance", ExifDirectory.TAG_SUBJECT_DISTANCE),
	};


	public void extractMixin(com.drew.metadata.Directory dir, MetadataExifFeature[] features, String metaMetadataTag)
	{
		Metadata mixin	= semanticsScope.getMetaMetadataRepository().constructByName(metaMetadataTag);
		if (mixin != null)
		{
			extractMetadata(dir, features, mixin);
			getDocument().addMixin(mixin);
		}
	}

	public void extractMetadata(com.drew.metadata.Directory dir, MetadataExifFeature[] features, ecologylab.semantics.metadata.Metadata metadata)
	{
		for (MetadataExifFeature feature: features)
		{
			feature.extract(metadata, dir);
		}
	}
	public static String getString(com.drew.metadata.Directory dir, int tag)
	{
		String result	= null;
		
		return result;
	}
	/**
	 * @param exifDirectory
	 * @return
	 */
	public Iterator<com.drew.metadata.Tag> printDirectory(com.drew.metadata.Directory exifDirectory)
	{
		Iterator<com.drew.metadata.Tag> tagList = exifDirectory.getTagIterator();
		while (tagList.hasNext())
		{
			com.drew.metadata.Tag tag	= tagList.next();
			System.out.print(tag + " | ");
//			if (tag.toString().toLowerCase().contains("gps"))
//				System.out.println("EUREKA EURKEA EUREKA! GPS: " + tag + ", ");
		}
		System.out.println();
		return tagList;
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
		result = closeImageInputStream();
		return result;
	}
	private boolean closeImageInputStream()
	{
		boolean result	= false;
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
	public synchronized void handleIoError(Throwable e)
	{
		if (imageReader != null)
			imageReader.abort();

		freeImageIOResources();
		super.error("Caught I/O Error while downloading image:");
		e.printStackTrace();
		recycle();
	}
}
