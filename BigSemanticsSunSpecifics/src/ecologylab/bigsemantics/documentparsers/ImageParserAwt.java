/**
 * 
 */
package ecologylab.bigsemantics.documentparsers;

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
import java.util.HashMap;
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

import ecologylab.bigsemantics.actions.SemanticsConstants;
import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
import ecologylab.bigsemantics.downloadcontrollers.DownloadController;
import ecologylab.bigsemantics.metadata.Metadata;
import ecologylab.bigsemantics.metadata.builtins.Image;
import ecologylab.bigsemantics.sensing.GisFeatures;
import ecologylab.bigsemantics.sensing.MetadataExifFeature;
import ecologylab.collections.CollectionTools;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;

/**
 * @author andruid
 */
public class ImageParserAwt extends ImageParser
{

  private static final String          MM_TAG_GPS_LOCATION    = "gps_location";

  private static final String          MM_TAG_CAMERA_SETTINGS = "camera_settings";

  private static final int             EXIF_TAG_ORIGINAL_DATE = 0x9003;

  static final DirectColorModel        ARGB_MODEL             = new DirectColorModel(32,
                                                                                     0x00ff0000,
                                                                                     0x0000ff00,
                                                                                     0xff,
                                                                                     0xff000000);

  static final PackedColorModel        RGB_MODEL              = new DirectColorModel(24,
                                                                                     0xff0000,
                                                                                     0xff00,
                                                                                     0xff,
                                                                                     0);

  static final int[]                   ARGB_MASKS             = { 0xff0000, 0xff00, 0xff, 0xff000000, };

  static final int[]                   RGB_MASKS              = { 0xff0000, 0xff00, 0xff, };

  static final int[]                   RGB_BANDS              = { 0, 1, 2 };

  public static final int              MIN_DIM                = 10;

  public static String EXIF_ELEMENT_TAG_NAME = "unknown";

  static final MetadataExifFeature        DATE_FEATURE             = new MetadataExifFeature(
   "creation_date",
   ExifDirectory.TAG_DATETIME);

  static final MetadataExifFeature        ORIG_DATE_FEATURE        = new MetadataExifFeature(
   "creation_date",
   ExifDirectory.TAG_DATETIME_ORIGINAL);

  static final MetadataExifFeature        CAMERA_MODEL_FEATURE     = new MetadataExifFeature(
   "model",
   ExifDirectory.TAG_MODEL);

  // // http://www.awaresystems.be/imaging/tiff/tifftags/privateifd/exif.html
  public static final MetadataExifFeature EXIF_METADATA_FEATURES[] =
                                                                   { CAMERA_MODEL_FEATURE,
      new MetadataExifFeature("orientation", ExifDirectory.TAG_ORIENTATION),
      new MetadataExifFeature("resolution", ExifDirectory.TAG_X_RESOLUTION),
      new MetadataExifFeature("exposure_time", ExifDirectory.TAG_EXPOSURE_TIME),
      new MetadataExifFeature("aperture", ExifDirectory.TAG_APERTURE),
      new MetadataExifFeature("shutter_speed", ExifDirectory.TAG_SHUTTER_SPEED),
      new MetadataExifFeature("subject_distance", ExifDirectory.TAG_SUBJECT_DISTANCE), };

  static Class                         cameraClass;

  static Class                         gpsClass;

  static final String[]                noAlphaMimeStrings     = { "image/jpeg", "image/bmp", };

  static final HashMap<String, String> noAlphaMimeMap         = CollectionTools.buildHashMapFromStrings(noAlphaMimeStrings);

  static
  {
    bindingParserMap.put(SemanticsConstants.IMAGE_PARSER, ImageParserAwt.class);
  }

  /** Patches a JPEG file that is missing a JFIF marker **/
  private static class PatchInputStream extends FilterInputStream
  {
    private static final int[] JFIF     = { 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00,
                                            0x01, 0x02, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00 };
  
    int                        position = 0;
  
    public PatchInputStream(InputStream in)
    {
      super(in);
    }
  
    @Override
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
  
    @Override
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

  private static ImageInputStream patch(InputStream in) throws IOException
  {
    in = new BufferedInputStream(in);
    in = new PatchInputStream(in);
    return ImageIO.createImageInputStream(in);
  }

  public static String getString(com.drew.metadata.Directory dir, int tag)
  {
    String result = null;
  
    return result;
  }

  ImageInputStream                     imageInputStream;

  ImageReader                          imageReader;

  public ImageParserAwt()
  {
    super();
  }

  protected void setSemanticsScope(SemanticsGlobalScope semanticsScope)
  {
    super.setSemanticsScope(semanticsScope);
    cameraClass = semanticsScope.getMetadataTypesScope().getClassByTag(MM_TAG_CAMERA_SETTINGS);
    gpsClass = semanticsScope.getMetadataTypesScope().getClassByTag(MM_TAG_GPS_LOCATION);
  }

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
      Debug.error(image, "ImageParserAwt failed for " + image.getLocation());
  }

  protected BufferedImage imageIORead(InputStream inputStream) throws IOException
  {
    BufferedImage bufferedImage = null;

    ImageIO.scanForPlugins();

    ParsedURL location = getDocument().getLocation();
    DownloadController downloadController = getDownloadController();
    if (downloadController.accessAndDownload(location))
    {
      boolean is_ico = false;
      // Mime type is checked to determine if the incoming file is a ICO file format
      if (downloadController.getMimeType() == "image/x-icon")
      {
        is_ico = true;
      }
      else if ("ico".equals(location.suffix()))
      {
        is_ico = true;
      }

      imageInputStream = ImageIO.createImageInputStream(inputStream);
      if (imageInputStream == null)
      {
        error("Cant open ImageInputStream for " + location);
      }
      else
      {
        Iterator<ImageReader> imageReadersIterator = ImageIO.getImageReaders(imageInputStream);
        if (!imageReadersIterator.hasNext())
          error("Cant get reader for " + location);
        else
        {
          while (imageReadersIterator.hasNext())
          {
            imageReader = imageReadersIterator.next();

            if (is_ico == true)
            {
              // if ICO file, force ICOReader to be used
              if ((imageReader.toString().contains("WBMPImageReader")))
              {
                continue;
              }
            }

            imageReader.setInput(imageInputStream, true, true);

            ImageReadParam param = imageReader.getDefaultReadParam();
            int width = imageReader.getWidth(0);
            int height = imageReader.getHeight(0);

            if ((width > MIN_DIM) && (height > MIN_DIM))
            {
              // try to setup the BufferedImage we use for the read to be structured
              // the way we like the data -- as an array of int[].
              // this means trying to find out about the image's structure,
              // in particular, if its rgb -- 3 color "bands", or argb alread -- 4 bands
              int readImageType = -1;
              // TODO this line is creating byte arrays :-(
              ImageTypeSpecifier rawImageType = imageReader.getRawImageType(0);
              // try to find out directly from ImageIO about the file's header
              if (rawImageType != null) // unfortunately this doesnt seem to work much for
                                        // URLConnection images
              {
                int rawNumBands = rawImageType.getNumBands();
                switch (rawNumBands)
                {
                case 4:
                  readImageType = BufferedImage.TYPE_INT_ARGB;
                  break;
                case 3:
                  readImageType = BufferedImage.TYPE_INT_RGB;
                  break;
                default:
                  if (rawImageType.getColorModel() instanceof IndexColorModel)
                  {
                    // readImageType= BufferedImage.TYPE_BYTE_INDEXED;
                  }
                  break;
                }
                // debug("gotRawImageType! numBands="+rawNumBands+ " readImageType="+readImageType);
              }
              // look in the URL itself
              else if (location.isNoAlpha()
                       || downloadController.getMimeType() != null
                       && noAlphaMimeMap.containsKey(downloadController.getMimeType()))
              {
                readImageType = BufferedImage.TYPE_INT_RGB;
              }
              int[] pixels = null;
              DataBufferInt dataBuffer = null;
              if (readImageType != -1)
              {
                ColorModel cm;
                int[] masks;
                if (readImageType == BufferedImage.TYPE_INT_RGB)
                {
                  cm = RGB_MODEL;
                  masks = RGB_MASKS;
                  // param.setDestinationBands(RGB_BANDS);
                }
                else
                {
                  cm = ARGB_MODEL;
                  masks = ARGB_MASKS;
                }
                SampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT,
                                                                  width,
                                                                  height,
                                                                  masks);
                int numPixels = width * height;
                pixels = new int[numPixels];
                dataBuffer = new DataBufferInt(pixels, numPixels);
                WritableRaster wr = Raster.createWritableRaster(sm, dataBuffer, null);
                bufferedImage = new BufferedImage(cm, wr, false, null);
                /*
                 * if (graphicsConfiguration != null) { int transparency = (readImageType ==
                 * BufferedImage.TYPE_INT_ARGB) ? Transparency.TRANSLUCENT : Transparency.OPAQUE;
                 * bufferedImage = graphicsConfiguration.createCompatibleImage(width, height,
                 * transparency); } else bufferedImage = new BufferedImage(width, height,
                 * readImageType);
                 */

                // Not needed since bufferedImage is filled by the read() call
                // param.setDestination(bufferedImage);
              }
              // read, using the BufferedImage we made, or the default
              // if we set result in the line above, we'll just get it back, so no problem.
              bufferedImage = imageReader.read(0, param);
              if (readImageType == -1)
              {

              }

              if (bufferedImage != null)
              {
                // throw new IOException("ImageParserAwt Cant read from imageReader for " +
                // location);

                while (imageReadersIterator.hasNext())
                {
                  ImageReader nextReader = imageReadersIterator.next();
                  debug("COOL: Freeing resources on additional readers! " + nextReader);
                  nextReader.reset();
                  nextReader.dispose();
                }
              }
            }

            if (bufferedImage != null)
            {
              String formatName = imageReader.getFormatName();
              if ("JPEG".equals(formatName))
                readMetadata(false);
              // desparate attempts to reduce referentiality :-)
              param.setDestination(null);
              param.setSourceBands(null);
              param.setDestinationBands(null);
              param.setDestinationType(null);
              param.setController(null);
              break;
            }

            imageInputStream = ImageIO.createImageInputStream(inputStream);
          }

          if (bufferedImage == null)
          {
            throw new IOException("ImageParserAwt Cant read from imageReader for " + location);
          }
        }
      }
    }
    freeImageIOResources();

    return bufferedImage;
  }

  private void readMetadata(boolean reread) throws IOException
  {
    try
    {
      IIOMetadata metadata = imageReader.getImageMetadata(0);

      String name = metadata.getNativeMetadataFormatName();
      IIOMetadataNode node = (IIOMetadataNode) metadata.getAsTree(name);
      // printTree(node);
      extractMetadataFeatures(node);
    }
    catch (javax.imageio.IIOException iioex)
    {
      // Crazy good workaround for java.imageio bug, from
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4924909
      if (!reread && iioex.getMessage() != null
          && iioex.getMessage().endsWith("without prior JFIF!"))
      {
        warning("Trying workaround for java bug");
        closeImageInputStream();
        InputStream newInputSteam = reConnect();
        imageInputStream = patch(newInputSteam);
        imageReader.setInput(imageInputStream);
        readMetadata(true);
        // IIOImage newImage = imageReader.readAll(0, null);
      }
      else
        warning("Couldn't extract metadata from image: " + iioex);
    }
    // byte[] iptc =(byte[]) iptcNode.getUserObject();
  }

  /**
   * @param node
   * @param exifTag
   */
  public void extractMetadataFeatures(IIOMetadataNode node)
  {
    NodeList unknownElements = node.getElementsByTagName(EXIF_ELEMENT_TAG_NAME);
    for (int i = 0; i < unknownElements.getLength(); i++)
    {
      IIOMetadataNode foundUnknownNode = (IIOMetadataNode) unknownElements.item(i);
      if ("225".equals(foundUnknownNode.getAttribute("MarkerTag")))
      {
        boolean dated = false;
        byte[] exifSegment = (byte[]) foundUnknownNode.getUserObject();

        final com.drew.metadata.Metadata exifMetadata = new com.drew.metadata.Metadata();
        new ExifReader(exifSegment).extract(exifMetadata);
        com.drew.metadata.Directory exifDir = exifMetadata.getDirectory(ExifDirectory.class);

        Image image = getDocument();
        boolean mixedIn = image.containsMixin(MM_TAG_CAMERA_SETTINGS);
        if (!mixedIn && !GisFeatures.containsGisMixin(image))
        {
          if (!dated && ORIG_DATE_FEATURE.extract(image, exifDir) == null)
          {
            dated = true;
            DATE_FEATURE.extract(image, exifDir);
          }
          if (!mixedIn && CAMERA_MODEL_FEATURE.getStringValue(exifDir) != null)
          {
            mixedIn = true;
            extractMixin(exifDir, EXIF_METADATA_FEATURES, MM_TAG_CAMERA_SETTINGS);
          }
          com.drew.metadata.Directory gpsDir = exifMetadata.getDirectory(GpsDirectory.class);
          Metadata gpsMixin = GisFeatures.extractMixin(gpsDir, getSemanticsScope(), image);
          Iterator<com.drew.metadata.Tag> gpsList = printDirectory(gpsDir);
          int qq = 33;
        }
      }
    }
  }

  public void extractMixin(com.drew.metadata.Directory dir, MetadataExifFeature[] features,
                           String metaMetadataTag)
  {
    Metadata mixin =
        getSemanticsScope().getMetaMetadataRepository().constructByName(metaMetadataTag);
    if (mixin != null)
    {
      extractMetadata(dir, features, mixin);
      getDocument().addMixin(mixin);
    }
  }

  public void extractMetadata(com.drew.metadata.Directory dir, MetadataExifFeature[] features,
                              ecologylab.bigsemantics.metadata.Metadata metadata)
  {
    for (MetadataExifFeature feature : features)
    {
      feature.extract(metadata, dir);
    }
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
      com.drew.metadata.Tag tag = tagList.next();
      System.out.print(tag + " | ");
      // if (tag.toString().toLowerCase().contains("gps"))
      // System.out.println("EUREKA EURKEA EUREKA! GPS: " + tag + ", ");
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
    boolean result = false;
    if (imageReader != null)
    {
      imageReader.reset(); // release all resources and set to initial state
      imageReader.dispose();
      imageReader = null;
      result = true;
    }
    result = closeImageInputStream();
    return result;
  }

  private boolean closeImageInputStream()
  {
    boolean result = false;
    if (imageInputStream != null)
    {
      try
      {
        imageInputStream.flush();
        imageInputStream.close();
        imageInputStream = null;
        result = true;
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      this.imageInputStream = null;
    }
    return result;
  }

  /**
   * @return false when the thing could not be stopped and a new thread started.
   */
  @Override
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
