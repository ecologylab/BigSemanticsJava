package com.drew.metadata.test;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;
import com.drew.metadata.jpeg.JpegCommentReader;
import com.sun.media.
import com.sun.image.codec.jpeg.JPEGDecodeParam;
import com.sun.*;

public class JpegMetadataReaderSunUtils
{

  public static Metadata readMetadata(JPEGDecodeParam decodeParam)
  {
    final Metadata metadata = new Metadata();

    /*
     * We should only really be seeing Exif in _data[0]... the 2D array exists because markers can
     * theoretically appear multiple times in the file.
     */
    // TODO test this method
    byte[][] exifSegment = decodeParam
        .getMarkerData(JPEGDecodeParam.APP1_MARKER);
    if (exifSegment != null && exifSegment[0].length > 0)
    {
      new ExifReader(exifSegment[0]).extract(metadata);
    }

    // similarly, use only the first IPTC segment
    byte[][] iptcSegment = decodeParam
        .getMarkerData(JPEGDecodeParam.APPD_MARKER);
    if (iptcSegment != null && iptcSegment[0].length > 0)
    {
      new IptcReader(iptcSegment[0]).extract(metadata);
    }

    // NOTE: Unable to utilise JpegReader for the SOF0 frame here, as the
    // decodeParam doesn't contain the byte[]

    // similarly, use only the first Jpeg Comment segment
    byte[][] jpegCommentSegment = decodeParam
        .getMarkerData(JPEGDecodeParam.COMMENT_MARKER);
    if (jpegCommentSegment != null && jpegCommentSegment[0].length > 0)
    {
      new JpegCommentReader(jpegCommentSegment[0]).extract(metadata);
    }

    return metadata;
  }

}
