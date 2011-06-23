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
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.GpsDirectory;

import ecologylab.concurrent.DownloadMonitor;
import ecologylab.generic.ReflectionTools;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.actions.SemanticActionsKeyWords;
import ecologylab.semantics.collecting.NewInfoCollector;
import ecologylab.semantics.metadata.Metadata;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.semantics.metadata.builtins.Image;

/**
 * @author andruid
 *
 */
public class ImageParserAwt extends DocumentParser<Image>
{
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
		cameraClass	= infoCollector.getMetadataTranslationScope().getClassByTag("camera_settings");
		gpsClass		= infoCollector.getMetadataTranslationScope().getClassByTag("gps_location");
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
		image.setParserResult(new ImageParserAwtResult(bufferedImage));
		image.setWidth(bufferedImage.getWidth());
		image.setHeight(bufferedImage.getHeight());
		
		//TODO for jpegs, extract EXIF metadata here
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
					readMetadata();
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
	
	private void readMetadata() throws IOException
	{
		IIOMetadata metadata	= imageReader.getImageMetadata(0);

		String name						= metadata.getNativeMetadataFormatName();
		IIOMetadataNode node	=(IIOMetadataNode) metadata.getAsTree(name);
//		printTree(node);
		extractMetadataFeatures(node);
//		byte[] iptc						=(byte[]) iptcNode.getUserObject();
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
	      boolean mixedIn			= false, dated = false;
				byte[] 	exifSegment	= (byte[]) foundUnknownNode.getUserObject();
				
        final com.drew.metadata.Metadata exifMetadata = new com.drew.metadata.Metadata();
      	new ExifReader(exifSegment).extract(exifMetadata);
      	com.drew.metadata.Directory exifDir = exifMetadata.getDirectory(ExifDirectory.class);
      	
      	if (!dated && ORIG_DATE_FEATURE.extractDate(getDocument(), exifDir) == null)
      	{
      		dated		= true;
      		DATE_FEATURE.extractDate(getDocument(), exifDir);
      	}
      	if (!mixedIn && (cameraClass != null) && CAMERA_MODEL_FEATURE.getStringValue(exifDir) != null)
      	{
      		mixedIn	= true;
//      		Metadata cameraMixin	= ReflectionTools.getInstance(cameraClass);
//      		extractMetadata(exifDirectory, EXIF_METADATA_FEATURES, cameraMixin);
      		extractMixin(exifDir, EXIF_METADATA_FEATURES, cameraClass);
      	}
      	com.drew.metadata.Directory gpsDir = exifMetadata.getDirectory(GpsDirectory.class);
      	String gpsLatitudeString = GPS_LATITUDE_FEATURE.getStringValue(gpsDir);
				if (gpsClass != null && gpsLatitudeString != null)
      	{
      		extractMixin(gpsDir, GPS_METADATA_FEATURES, gpsClass);
      	}
//    	Iterator<com.drew.metadata.Tag> exifList = printDirectory(exifDirectory);
      	Iterator<com.drew.metadata.Tag> gpsList = printDirectory(gpsDir);
      	int qq = 33;
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

	public static final MetadataExifFeature GPS_LATITUDE_FEATURE		= new MetadataExifFeature("latitude", GpsDirectory.TAG_GPS_LATITUDE);
	public static final MetadataExifFeature GPS_METADATA_FEATURES[]	=
	{
		GPS_LATITUDE_FEATURE,
		new MetadataExifFeature("longitude", GpsDirectory.TAG_GPS_LONGITUDE),
	};
	public void extractMixin(com.drew.metadata.Directory dir, MetadataExifFeature[] features, Class<Metadata> metadataClass)
	{
		Metadata mixin	= ReflectionTools.getInstance(metadataClass);
		extractMetadata(dir, features, mixin);
		getDocument().addMixin(mixin);
	}
	public void extractMetadata(com.drew.metadata.Directory dir, MetadataExifFeature[] features, ecologylab.semantics.metadata.Metadata metadata)
	{
		for (MetadataExifFeature feature: features)
		{
			feature.extractString(metadata, dir);
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
  public static void printTree(Node doc) 
  {
  	printTree(doc, 0);
  }
  public static void printTree(Node node, int level) 
  {
    try 
    {
    	for (int i=0; i< level; i++)
    		System.out.print('\t');
    	
      System.out.print("<" + node.getNodeName());
      NamedNodeMap attrMap = node.getAttributes();
      for (int i = 0; i < attrMap.getLength(); i++) 
      {
        Node attr = attrMap.item(i);
        String attrName = attr.getNodeName();
				System.out.print(" " + attrName + "=\"" + attr.getNodeValue() + '"');
      }
      String value	= node.getNodeValue();
      if (value != null)
      	System.out.print(value);
      System.out.print(">");

      NodeList nl = node.getChildNodes();
      int numChildren = nl.getLength();
      if (numChildren > 0)
      {
        System.out.print("\n");
				for (int i = 0; i < numChildren; i++) 
	      {
	        Node childNode = nl.item(i);
	        printTree(childNode, level + 1);
	      }
	    	for (int i=0; i< level; i++)
	    		System.out.print('\t');
      }
      System.out.println("</" + node.getNodeName() + ">");
    } catch (Throwable e) 
    {
      System.out.println("Cannot print!! " + e.getMessage());
    }
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
